package k25.kaatokerho.service;

import java.util.*;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import k25.kaatokerho.domain.GP;
import k25.kaatokerho.domain.Keilaaja;
import k25.kaatokerho.domain.KeilaajaKausi;
import k25.kaatokerho.domain.KeilaajaKausiRepository;
import k25.kaatokerho.domain.KuppiksenKunkku;
import k25.kaatokerho.domain.KuppiksenKunkkuRepository;
import k25.kaatokerho.domain.Tulos;
import k25.kaatokerho.domain.TulosRepository;

/**
 * Kuppiksen Kunkku – palvelu
 *
 * Säännöt (tiivistettynä):
 * - Puolustaja = edellisen GP:n KK-ottelun voittaja.
 * - Ottelu pelataan AINA. Jos alkuperäinen puolustaja on poissa tai vyö on unohtunut,
 *   puolustusoikeus siirtyy yhden pykälän alas: haastajalistan #1 -> puolustaja,
 *   ja seuraava paikalla oleva -> haastaja.
 * - Haastajalista: paras sarja DESC, huonompi sarja DESC, keilaajaId ASC (determinismi).
 * - Täydellinen tasapeli ottelussa -> IllegalStateException -> UI valitsee voittajan.
 * - Voittajalle +1 kausipiste.
 */
@Service
public class KuppiksenKunkkuService {

    private static final Logger log = LoggerFactory.getLogger(KuppiksenKunkkuService.class);

    private final KuppiksenKunkkuRepository kuppiksenKunkkuRepository;
    private final TulosRepository tulosRepository;
    private final KeilaajaKausiRepository keilaajaKausiRepository;

    // Thymeleaf / diagnostiikka
    private final List<KuppiksenKunkku> kaudenKaksintaistelut = new ArrayList<>();
    private final Map<Long, List<Keilaaja>> haastajalistatByGpId = new HashMap<>();

    public KuppiksenKunkkuService(KuppiksenKunkkuRepository kuppiksenKunkkuRepository,
                                  TulosRepository tulosRepository,
                                  KeilaajaKausiRepository keilaajaKausiRepository) {
        this.kuppiksenKunkkuRepository = kuppiksenKunkkuRepository;
        this.tulosRepository = tulosRepository;
        this.keilaajaKausiRepository = keilaajaKausiRepository;
    }

    @Transactional
    public void kasitteleKuppiksenKunkku(GP gp, KuppiksenKunkku edellinen, boolean vyoUnohtui) {
        // 1) Kerää osallistuneet
        final List<Tulos> osallistuneet = gp.getTulokset() == null
                ? List.of()
                : gp.getTulokset().stream().filter(Tulos::getOsallistui).toList();
        if (osallistuneet.isEmpty()) {
            return; // ei ottelua
        }

        // 2) Muodosta haastajalista ja poista duplikaatit säilyttäen järjestys
        List<Keilaaja> jarjestetty = muodostaHaastajalista(osallistuneet);
        jarjestetty = poistaDuplikaatitSailyttaenJarjestys(jarjestetty);
        if (jarjestetty.isEmpty()) return;
        haastajalistatByGpId.put(gp.getGpId(), jarjestetty);

        // 3) Valitse puolustaja ja haastaja
        final Keilaaja puolustaja;
        Keilaaja haastaja;

        log.debug("=== Kuppiksen Kunkku GP#{} ===", gp.getJarjestysnumero());
        log.debug("Haastajalista (järjestetty): {}",
                jarjestetty.stream()
                        .map(k -> k.getKeilaajaId() + ":" + k.getEtunimi() + " " + k.getSukunimi())
                        .collect(Collectors.toList()));
        log.debug("Edellinen hallitseva: {}",
                (edellinen == null)
                        ? "ei edellistä"
                        : (edellinen.getHallitseva().getKeilaajaId() + ":" + edellinen.getHallitseva().getEtunimi()));
        log.debug("Vyö unohtui: {}", vyoUnohtui);

        if (edellinen == null || gp.getJarjestysnumero() == 1) {
            // Kauden ensimmäinen GP: listan #1 puolustaa, seuraava paikalla oleva haastaa
            puolustaja = jarjestetty.get(0);
            haastaja = etsiSeuraavaPaikallaOleva(osallistuneet, jarjestetty, Set.of(puolustaja.getKeilaajaId()));
        } else {
            final Keilaaja edellinenVoittaja = edellinen.getHallitseva();
            final Long edId = edellinenVoittaja != null ? edellinenVoittaja.getKeilaajaId() : null;
            final boolean edellinenPaikalla = onPaikalla(osallistuneet, edId);

            if (edellinenPaikalla && !vyoUnohtui) {
                // Normaalitilanne: edellinen voittaja puolustaa
                puolustaja = edellinenVoittaja;
                haastaja = etsiSeuraavaPaikallaOleva(osallistuneet, jarjestetty, Set.of(edId));
            } else {
                // Puolustaja poissa TAI vyö unohtui -> puolustusoikeus siirtyy yksi pykälä alas
                // Uusi puolustaja = ensimmäinen paikalla oleva, joka ei ole edellinen voittaja
                puolustaja = etsiSeuraavaPaikallaOleva(osallistuneet, jarjestetty,
                        (edId == null) ? Set.of() : Set.of(edId));
                // Uusi haastaja = seuraava paikalla oleva, joka ei ole (edellinen) eikä uusi puolustaja
                Set<Long> exclude = new HashSet<>();
                if (edId != null) exclude.add(edId);
                if (puolustaja != null && puolustaja.getKeilaajaId() != null) {
                    exclude.add(puolustaja.getKeilaajaId());
                }
                haastaja = etsiSeuraavaPaikallaOleva(osallistuneet, jarjestetty, exclude);
            }
        }

        // Turvavyö: älä koskaan pelaa itseäsi vastaan
        if (haastaja != null && Objects.equals(haastaja.getKeilaajaId(), puolustaja.getKeilaajaId())) {
            haastaja = etsiSeuraavaPaikallaOleva(osallistuneet, jarjestetty, Set.of(puolustaja.getKeilaajaId()));
        }

        log.debug("Valittu puolustaja: {}",
                puolustaja == null ? "null" : puolustaja.getKeilaajaId() + ":" + puolustaja.getEtunimi());
        log.debug("Valittu haastaja: {}",
                haastaja == null ? "null" : haastaja.getKeilaajaId() + ":" + haastaja.getEtunimi());

        // 4) Ratkaise ottelu
        final Keilaaja voittaja;
        if (haastaja == null) {
            // Vain yksi osallistuja -> “walkover”
            voittaja = puolustaja;
        } else {
            // Diagnoosi: tulosarvot ennen ratkaisua
            var tP = tulosRepository.findByGpAndKeilaaja(gp, puolustaja);
            var tH = tulosRepository.findByGpAndKeilaaja(gp, haastaja);
            int pP = Math.max(nullSafe(tP.getSarja1()), nullSafe(tP.getSarja2()));
            int hP = Math.min(nullSafe(tP.getSarja1()), nullSafe(tP.getSarja2()));
            int pH = Math.max(nullSafe(tH.getSarja1()), nullSafe(tH.getSarja2()));
            int hH = Math.min(nullSafe(tH.getSarja1()), nullSafe(tH.getSarja2()));
            log.debug("Ottelu: puolustaja {} (paras {}, huonompi {}) vs haastaja {} (paras {}, huonompi {})",
                    puolustaja.getKeilaajaId(), pP, hP, haastaja.getKeilaajaId(), pH, hH);

            voittaja = ratkaiseOttelu(gp, puolustaja, haastaja);
        }

        // 5) Talleta rivi ja +1 piste voittajalle
        lisaaKaksintaistelu(gp, voittaja, haastaja, vyoUnohtui);
        lisaaYksiSarjataulukkoPiste(voittaja, gp);
    }

    /**
     * Haastajalista järjestyksessä:
     * 1) paras sarja DESC
     * 2) huonompi sarja DESC
     * 3) keilaajaId ASC (determinismi)
     */
    List<Keilaaja> muodostaHaastajalista(List<Tulos> osallistuneet) {
        record R(Keilaaja k, int paras, int huonompi, Long id) {}
        return osallistuneet.stream()
                .map(t -> new R(
                        t.getKeilaaja(),
                        Math.max(nullSafe(t.getSarja1()), nullSafe(t.getSarja2())),
                        Math.min(nullSafe(t.getSarja1()), nullSafe(t.getSarja2())),
                        t.getKeilaaja() != null ? t.getKeilaaja().getKeilaajaId() : null
                ))
                .sorted((a, b) -> {
                    int cmp = Integer.compare(b.paras, a.paras);
                    if (cmp != 0) return cmp;
                    cmp = Integer.compare(b.huonompi, a.huonompi);
                    if (cmp != 0) return cmp;
                    // determinismi: pienin ID ensin (nullit lopuksi)
                    if (a.id == null && b.id == null) return 0;
                    if (a.id == null) return 1;
                    if (b.id == null) return -1;
                    return Long.compare(a.id, b.id);
                })
                .map(R::k)
                .collect(Collectors.toList());
    }

    /**
     * Poistaa duplikaatit säilyttäen järjestyksen. Vertailu keilaajaId:llä.
     */
    List<Keilaaja> poistaDuplikaatitSailyttaenJarjestys(List<Keilaaja> lista) {
        List<Keilaaja> uniikit = new ArrayList<>();
        Set<Long> seen = new HashSet<>();
        for (Keilaaja k : lista) {
            Long id = (k != null) ? k.getKeilaajaId() : null;
            if (id == null || seen.add(id)) {
                uniikit.add(k);
            }
        }
        return uniikit;
    }

    /**
     * Ratkaisee ottelun:
     * - Ensin paras sarja DESC
     * - Sitten huonompi sarja DESC
     * - Täydellinen tasapeli -> IllegalStateException
     */
    Keilaaja ratkaiseOttelu(GP gp, Keilaaja k1, Keilaaja k2) {
        Tulos t1 = tulosRepository.findByGpAndKeilaaja(gp, k1);
        Tulos t2 = tulosRepository.findByGpAndKeilaaja(gp, k2);
        if (t1 == null || t2 == null) {
            throw new IllegalStateException("Ottelun ratkaisu epäonnistui: tulos puuttuu.");
        }

        int p1 = Math.max(nullSafe(t1.getSarja1()), nullSafe(t1.getSarja2()));
        int p2 = Math.max(nullSafe(t2.getSarja1()), nullSafe(t2.getSarja2()));
        if (p1 != p2) return p1 > p2 ? k1 : k2;

        int h1 = Math.min(nullSafe(t1.getSarja1()), nullSafe(t1.getSarja2()));
        int h2 = Math.min(nullSafe(t2.getSarja1()), nullSafe(t2.getSarja2()));
        if (h1 != h2) return h1 > h2 ? k1 : k2;

        throw new IllegalStateException("Täydellinen tasapeli – valitse voittaja käyttöliittymässä.");
    }

    /**
     * Palauttaa listalta ensimmäisen paikalla olevan keilaajan, jota ei ole excludeIds-joukossa.
     */
    Keilaaja etsiSeuraavaPaikallaOleva(List<Tulos> osallistuneet, List<Keilaaja> jarjestetty, Set<Long> excludeIds) {
        for (Keilaaja k : jarjestetty) {
            if (k == null || k.getKeilaajaId() == null) continue;
            Long id = k.getKeilaajaId();
            if (excludeIds != null && excludeIds.contains(id)) continue;
            if (onPaikalla(osallistuneet, id)) return k;
        }
        return null;
    }

    /**
     * Onko annettu keilaajaId paikalla tämän GP:n tuloksissa.
     */
    boolean onPaikalla(List<Tulos> osallistuneet, Long keilaajaId) {
        if (keilaajaId == null) return false;
        for (Tulos t : osallistuneet) {
            Keilaaja k = t.getKeilaaja();
            if (k != null && Objects.equals(k.getKeilaajaId(), keilaajaId)) {
                return true;
            }
        }
        return false;
    }

    private int nullSafe(Integer val) {
        return val == null ? 0 : val;
    }

    void lisaaKaksintaistelu(GP gp, Keilaaja voittaja, Keilaaja haastaja, boolean vyoUnohtui) {
        KuppiksenKunkku kk = new KuppiksenKunkku();
        kk.setGp(gp);
        kk.setHallitseva(voittaja);
        kk.setHaastaja(haastaja);
        kk.setVyoUnohtui(vyoUnohtui);
        kuppiksenKunkkuRepository.save(kk);
        kaudenKaksintaistelut.add(kk);
    }

    void lisaaYksiSarjataulukkoPiste(Keilaaja voittaja, GP gp) {
        if (voittaja == null || gp == null || gp.getKausi() == null) return;
        Optional<KeilaajaKausi> kkOpt = keilaajaKausiRepository.findByKeilaajaAndKausi(voittaja, gp.getKausi());
        kkOpt.ifPresent(kk -> {
            kk.setKaudenPisteet(kk.getKaudenPisteet() + 1.0);
            keilaajaKausiRepository.save(kk);
        });
    }

    // --------- Getterit Thymeleafia/testejä varten ---------

    public List<KuppiksenKunkku> getKaudenKaksintaistelut() {
        return Collections.unmodifiableList(kaudenKaksintaistelut);
    }

    public List<Keilaaja> getHaastajalista(GP gp) {
        return haastajalistatByGpId.getOrDefault(gp.getGpId(), Collections.emptyList());
    }
}
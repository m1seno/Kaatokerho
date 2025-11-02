package k25.kaatokerho.service;

import java.util.*;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import k25.kaatokerho.domain.GP;
import k25.kaatokerho.domain.Keilaaja;
import k25.kaatokerho.domain.KeilaajaKausiRepository;
import k25.kaatokerho.domain.KuppiksenKunkku;
import k25.kaatokerho.domain.KuppiksenKunkkuRepository;
import k25.kaatokerho.domain.Tulos;
import k25.kaatokerho.domain.TulosRepository;

/**
 * Kuppiksen Kunkku – palvelu
 *
 * Säännöt (tiivistettynä):
 * - Puolustaja = edellisen GP:n KK-ottelun voittaja (fallback: edellisen KK:n puolustaja).
 * - Ottelu pelataan AINA. Jos alkuperäinen puolustaja on poissa tai vyö on unohtunut,
 *   puolustusoikeus siirtyy yhden pykälän alas: haastajalistan #1 -> puolustaja,
 *   ja seuraava paikalla oleva -> haastaja.
 * - Haastajalista GPₙ:lle rakennetaan GPₙ₋₁:n tuloksista (eka GP käyttää omaa),
 *   järjestys: paras DESC, huonompi DESC, keilaajaId ASC (determinismi),
 *   JA listalta poistetaan puolustaja (UI:lle selkeä “mandaatti-haastaja”).
 * - Täydellinen tasapeli ottelussa -> IllegalStateException -> UI valitsee voittajan.
 * - Voittajalle +1 kausipiste.
 */
@Service
public class KuppiksenKunkkuService {

    private static final Logger log = LoggerFactory.getLogger(KuppiksenKunkkuService.class);

    private final KuppiksenKunkkuRepository kkRepo;
    private final TulosRepository tulosRepo;
    private final KeilaajaKausiRepository kkSeasonRepo;

    // Haastajalista muistissa GP:tä kohti (ei DB:hen)
    private final Map<Long, List<Keilaaja>> haastajalistaByGp = new HashMap<>();

    public KuppiksenKunkkuService(KuppiksenKunkkuRepository kkRepo,
                                  TulosRepository tulosRepo,
                                  KeilaajaKausiRepository kkSeasonRepo) {
        this.kkRepo = kkRepo;
        this.tulosRepo = tulosRepo;
        this.kkSeasonRepo = kkSeasonRepo;
    }

    @Transactional
    public void kasitteleKuppiksenKunkku(GP gp, KuppiksenKunkku edellinen, boolean vyoUnohtui) {
        // Osallistujat (vain paikalla olleet NYKYISESSÄ GP:ssä) — haetaan repositorysta
        List<Tulos> osallistuneet = tulosRepo.findByGp(gp).stream()
                .filter(Tulos::getOsallistui)
                .toList();
        if (osallistuneet.isEmpty()) return;

        log.debug("=== KK START GP#{} (pvm={}) ===", gp.getJarjestysnumero(), gp.getPvm());
        log.debug("vyöUnohtui = {}", vyoUnohtui);

        // 1) Selvitä puolustaja tälle GP:lle JA rakenna haastajalista oikeasta lähteestä
        Keilaaja puolustaja;
        List<Keilaaja> haastajalistaForThisGp;
        GP listanLahdeGp; // lokitusta varten

        if (edellinen == null || gp.getJarjestysnumero() == 1) {
            // Kauden eka GP: puolustaja = tämän GP:n rankingin ykkönen
            List<Tulos> tuloksetTamaGp = tulosRepo.findByGp(gp);
            List<Keilaaja> rankThisGp = muodostaHaastajalistaTuloksista(tuloksetTamaGp, null);
            if (rankThisGp.isEmpty()) return;
            puolustaja = rankThisGp.get(0);
            // Haastajalista UI:lle = ilman puolustajaa
            haastajalistaForThisGp = rankThisGp.stream().skip(1).toList();
            listanLahdeGp = gp;
        } else {
            // Normaalit GP:t: puolustaja = edellisen KK:n voittaja (fallback: edellisen KK:n puolustaja)
            Keilaaja edVoittaja = edellinen.getVoittaja();
            if (edVoittaja == null) {
                edVoittaja = edellinen.getPuolustaja();
            }
            puolustaja = edVoittaja;

            // Haastajalista GPₙ:lle muodostetaan GPₙ₋₁:n tuloksista ja poistetaan puolustaja
            GP edellinenGp = edellinen.getGp();
            List<Tulos> tuloksetEdellisesta = tulosRepo.findByGp(edellinenGp);
            haastajalistaForThisGp = muodostaHaastajalistaTuloksista(tuloksetEdellisesta, puolustaja);
            listanLahdeGp = edellinenGp;
        }

        // Talleta UI:lle näytettävä lista (ei sisällä puolustajaa)
        haastajalistaByGp.put(gp.getGpId(), haastajalistaForThisGp);

        // Debug: näytä haastajalista, jossa sarjat tulevat listan lähde-GP:stä
        log.debug("Haastajalista GP#{} (lähde={} -> ilman puolustajaa): {}",
                gp.getJarjestysnumero(),
                (listanLahdeGp == gp ? "tämä GP" : "edellinen GP"),
                formatHaastajalista(listanLahdeGp, haastajalistaForThisGp));

        // 2) Valitse lopullinen puolustaja & haastaja NYKYISEEN GP:hen läsnäolot huomioiden
        Keilaaja haastaja;
        boolean puolustajaPaikalla = onPaikalla(osallistuneet, puolustaja);
        if (puolustajaPaikalla && !vyoUnohtui) {
            // puolustaja pysyy -> haastajaksi listan kärjestä ensimmäinen paikalla oleva
            haastaja = etsiSeuraavaPaikallaOleva(osallistuneet, haastajalistaForThisGp, null);
        } else {
            // puolustaja vaihtuu -> uusi puolustaja = listan 1. paikalla oleva
            Keilaaja uusiPuolustaja = etsiSeuraavaPaikallaOleva(osallistuneet, haastajalistaForThisGp, null);
            // haastajaksi seuraava paikalla oleva ≠ uusi puolustaja
            Keilaaja uusiHaastaja = etsiSeuraavaPaikallaOleva(osallistuneet, haastajalistaForThisGp, uusiPuolustaja);
            puolustaja = uusiPuolustaja;
            haastaja = uusiHaastaja;
        }

        // Debuggaus (sarjat nykyisestä GP:stä, koska ottelu pelataan tässä GP:ssä)
        log.debug("Valittu puolustaja: {} ({})", n(puolustaja), sarjatFor(gp, puolustaja));
        log.debug("Valittu haastaja  : {} ({})", n(haastaja), sarjatFor(gp, haastaja));

        // 3) Ratkaise ottelu → VOITTAJA
        Keilaaja voittaja;
        if (haastaja == null) {
            voittaja = puolustaja; // vain yksi kelvollinen pelaaja
        } else {
            voittaja = ratkaiseOttelu(gp, puolustaja, haastaja);
        }

        // Debuggaus
        log.debug("VOITTAJA: {} ({})", n(voittaja), sarjatFor(gp, voittaja));

        // 4) Tallenna yksi rivi: puolustaja, haastaja, voittaja, vyöUnohtui
        tallennaKkRivi(gp, puolustaja, haastaja, voittaja, vyoUnohtui);

        // 5) +1 piste voittajalle
        lisaaYksiPisteSarjataulukkoon(voittaja, gp);

        // Debuggaus
        log.debug("Persistoitu KK (gpId={}, puolustaja={}, haastaja={}, voittaja={}, vyöUnohtui={})",
                gp.getGpId(), n(puolustaja), n(haastaja), n(voittaja), vyoUnohtui);
        log.debug("=== KK END   GP#{} ===", gp.getJarjestysnumero());
    }

    // ---------- apurit ----------

    /**
     * Rakentaa järjestetyn haastajalistan annetun GP:n tuloksista (haetaan repositorysta).
     * Järjestys: paras DESC, huonompi DESC, keilaajaId ASC.
     * Jos exclude != null, poistetaan hänet listalta (esim. puolustaja).
     */
    private List<Keilaaja> muodostaHaastajalistaGpsta(GP gp, Keilaaja exclude) {
        List<Tulos> tulokset = tulosRepo.findByGp(gp);
        return muodostaHaastajalistaTuloksista(tulokset, exclude);
    }

    /**
     * Yleismuotoinen rakentaja: tee ranking annetusta tuloslistasta.
     */
    private List<Keilaaja> muodostaHaastajalistaTuloksista(List<Tulos> tulokset, Keilaaja exclude) {
        if (tulokset == null) return List.of();
        record R(Keilaaja k, int paras, int huonompi, Long id) {}
        return tulokset.stream()
            .filter(Objects::nonNull)
            .filter(Tulos::getOsallistui)
            .map(t -> new R(
                    t.getKeilaaja(),
                    Math.max(nullSafe(t.getSarja1()), nullSafe(t.getSarja2())),
                    Math.min(nullSafe(t.getSarja1()), nullSafe(t.getSarja2())),
                    t.getKeilaaja().getKeilaajaId()))
            .sorted((a,b) -> {
                int cmp = Integer.compare(b.paras, a.paras);
                if (cmp != 0) return cmp;
                cmp = Integer.compare(b.huonompi, a.huonompi);
                if (cmp != 0) return cmp;
                return Long.compare(a.id, b.id);
            })
            .map(R::k)
            .filter(k -> exclude == null || !k.equals(exclude))
            .toList();
    }

    private Keilaaja ratkaiseOttelu(GP gp, Keilaaja k1, Keilaaja k2) {
        Tulos t1 = tulosRepo.findByGpAndKeilaaja(gp, k1);
        Tulos t2 = tulosRepo.findByGpAndKeilaaja(gp, k2);
        if (t1 == null || t2 == null)
            throw new IllegalStateException("Tulokset puuttuvat.");

        int p1 = Math.max(ns(t1.getSarja1()), ns(t1.getSarja2()));
        int p2 = Math.max(ns(t2.getSarja1()), ns(t2.getSarja2()));
        if (p1 != p2) return p1 > p2 ? k1 : k2;

        int h1 = Math.min(ns(t1.getSarja1()), ns(t1.getSarja2()));
        int h2 = Math.min(ns(t2.getSarja1()), ns(t2.getSarja2()));
        if (h1 != h2) return h1 > h2 ? k1 : k2;

        // Täydellinen tasapeli → UI valitsee voittajan (kaadot/paikat puuttuvat)
        throw new IllegalStateException("Täydellinen tasapeli – valitse voittaja käyttöliittymässä.");
    }

    private Keilaaja etsiSeuraavaPaikallaOleva(List<Tulos> osallistuneet,
                                               List<Keilaaja> jarjestys,
                                               Keilaaja pois) {
        return jarjestys.stream()
                .filter(k -> pois == null || !k.equals(pois))
                .filter(k -> onPaikalla(osallistuneet, k))
                .findFirst().orElse(null);
    }

    private boolean onPaikalla(List<Tulos> osallistuneet, Keilaaja k) {
        return osallistuneet.stream().anyMatch(t -> t.getKeilaaja().equals(k));
    }

    private int ns(Integer v) {
        return v == null ? 0 : v;
    }

    private void tallennaKkRivi(GP gp, Keilaaja puolustaja, Keilaaja haastaja,
                                Keilaaja voittaja, boolean vyoUnohtui) {
        KuppiksenKunkku kk = new KuppiksenKunkku();
        kk.setGp(gp);
        kk.setPuolustaja(puolustaja);
        kk.setHaastaja(haastaja);
        kk.setVoittaja(voittaja);
        kk.setVyoUnohtui(vyoUnohtui);
        kkRepo.save(kk);
    }

    private void lisaaYksiPisteSarjataulukkoon(Keilaaja voittaja, GP gp) {
        if (voittaja == null || gp == null || gp.getKausi() == null) return;
        kkSeasonRepo.findByKeilaajaAndKausi(voittaja, gp.getKausi()).ifPresent(kk -> {
            kk.setKaudenPisteet(kk.getKaudenPisteet() + 1.0);
            kkSeasonRepo.save(kk);
        });
    }

    // Julkinen getter UI:lle / testeille, ei DB-kirjoitusta
    public List<Keilaaja> getHaastajalista(GP gp) {
        return haastajalistaByGp.getOrDefault(gp.getGpId(), List.of());
    }

    // --- DEBUG/APU: nimet ja sarjat lokitusta varten ---

    /** Palauttaa turvallisesti nimen: "id:Etunimi Sukunimi" tai "null". */
    private String n(Keilaaja k) {
        return (k == null) ? "null" : (k.getKeilaajaId() + ":" + k.getEtunimi() + " " + k.getSukunimi());
    }

    /** Palauttaa "paras XXX, huonompi YYY" annetulle keilaajalle tästä GP:stä. */
    private String sarjatFor(GP gp, Keilaaja k) {
        if (gp == null || k == null) return "—";
        Tulos t = tulosRepo.findByGpAndKeilaaja(gp, k);
        if (t == null) return "—";
        int s1 = nullSafe(t.getSarja1());
        int s2 = nullSafe(t.getSarja2());
        int paras = Math.max(s1, s2);
        int huonompi = Math.min(s1, s2);
        return "paras " + paras + ", huonompi " + huonompi;
    }

    /** Muotoilee haastajalistan lokiin tyyliin: [id:Nimi (paras,huonompi), ...]. */
    private String formatHaastajalista(GP gp, List<Keilaaja> lista) {
        if (lista == null || lista.isEmpty()) return "[]";
        List<String> parts = new ArrayList<>();
        for (Keilaaja k : lista) {
            String sj = sarjatFor(gp, k);
            parts.add(k.getKeilaajaId() + ":" + k.getEtunimi() + " " + k.getSukunimi() + " (" + sj + ")");
        }
        return parts.toString();
    }

    /** Null-suojattu Integer -> int (null => 0). */
    private int nullSafe(Integer val) {
        return val == null ? 0 : val;
    }
}
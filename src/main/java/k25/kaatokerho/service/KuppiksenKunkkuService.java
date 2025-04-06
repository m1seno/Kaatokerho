package k25.kaatokerho.service;

import k25.kaatokerho.domain.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

//Vastaa kuppiksenkunkku-taulun käsittelystä
@Service
public class KuppiksenKunkkuService {

    private final KuppiksenKunkkuRepository kuppiksenKunkkuRepository;
    private final TulosRepository tulosRepository;

    @Autowired
    public KuppiksenKunkkuService(KuppiksenKunkkuRepository kuppiksenKunkkuRepository,
            TulosRepository tulosRepository) {
        this.kuppiksenKunkkuRepository = kuppiksenKunkkuRepository;
        this.tulosRepository = tulosRepository;
    }

    private final List<KuppiksenKunkku> kaudenKaksintaistelut = new ArrayList<>();
    private final Map<GP, List<Keilaaja>> haastajalistat = new HashMap<>();

    // Tämän metodin logiikka meni lopulta niin monimutkaiseksi, että tehty lähes
    // kokonaan yhteistyössä chatGPT:n kanssa
    public void kasitteleKuppiksenKunkku(GP gp, KuppiksenKunkku edellinen) {
        // Haetaan vain GP:hen osallistuneet tulokset
        List<Tulos> tulokset = gp.getTulokset().stream()
                .filter(Tulos::getOsallistui)
                .toList();

        // Jos kyseessä on ensimmäinen GP tai ei ole aiempaa hallitsevaa
        if (gp.getJarjestysnumero() == 1 || edellinen == null) {
            Voittajat v = haeVoittajaJaHaastaja(tulokset);

            // Talletetaan haastajalista Thymeleafin käyttöä varten
            haastajalistat.put(gp, List.of(v.haastaja()));

            // Ensimmäisessä kisassa kaikki oletetaan paikalla ja vyö mukana
            lisaaKaksintaistelu(gp, v.voittaja(), v.haastaja(),
                    false, true, true);
            return;
        }

        // Aiempi hallitseva keilaaja
        Keilaaja edellinenVoittaja = edellinen.getHallitseva();
        // Tieto unohtuiko vyö kotiin
        boolean vyoUnohtui = edellinen.getVyoUnohtui();

        // Muodostetaan haastajalista (paras–huonoin sarjat)
        List<Keilaaja> haastajat = muodostaHaastajalista(tulokset, edellinenVoittaja);
        haastajalistat.put(gp, haastajat); // Thymeleafia varten

        // Tarkistetaan onko hallitseva keilaaja paikalla
        boolean hallitsevaPaikalla = tulokset.stream()
                .anyMatch(t -> t.getKeilaaja().equals(edellinenVoittaja));

        // Oletetaan että hallitseva säilyttää vyön, jos ei muuta
        Keilaaja uusiVoittaja = edellinenVoittaja;

        // Holder-luokan kautta hallitaan haastajan valintaa lambdoissa
        final Keilaaja[] lopullinenHaastaja = new Keilaaja[1];

        // Jos hallitseva on paikalla ja vyö mukana, voidaan käydä ottelu
        if (hallitsevaPaikalla && !haastajat.isEmpty() && !vyoUnohtui) {

            // Tarkistetaan onko ensisijainen haastaja paikalla
            Keilaaja ensisijainenHaastaja = haastajat.get(0);
            boolean ensisijainenPaikalla = tulokset.stream()
                    .anyMatch(t -> t.getKeilaaja().equals(ensisijainenHaastaja));

            // Jos paikalla, otetaan se; muuten etsitään seuraava paikalla oleva haastaja
            lopullinenHaastaja[0] = ensisijainenPaikalla
                    ? ensisijainenHaastaja
                    : haastajat.stream()
                            .filter(h -> !h.equals(edellinenVoittaja))
                            .filter(h -> tulokset.stream().anyMatch(t -> t.getKeilaaja().equals(h)))
                            .findFirst().orElse(null);

            // Jos löytyi haastaja, verrataan suorituksia ja valitaan voittaja
            if (lopullinenHaastaja[0] != null) {
                uusiVoittaja = valitseVoittaja(gp, edellinenVoittaja, lopullinenHaastaja[0]);
            }
        } else {
            // Jos hallitseva ei ole paikalla tai vyö unohtui, vyö siirtyy haastajalistan
            // kärjessä olevalle
            if (!haastajat.isEmpty()) {
                uusiVoittaja = haastajat.stream()
                        .filter(h -> tulokset.stream().anyMatch(t -> t.getKeilaaja().equals(h)))
                        .findFirst().orElse(edellinenVoittaja); // fallback: jos ei paikalla olevia, säilyy
                                                                // hallitsijalla
            }
        }

        // Tarkistetaan oliko haastaja lopulta paikalla (jos valittiin)
        boolean haastajaPaikalla = lopullinenHaastaja[0] != null &&
                tulokset.stream().anyMatch(t -> t.getKeilaaja().equals(lopullinenHaastaja[0]));

        // Tallennetaan kaksintaistelun tiedot tietokantaan
        lisaaKaksintaistelu(gp, uusiVoittaja, lopullinenHaastaja[0],
                vyoUnohtui, hallitsevaPaikalla, haastajaPaikalla);
    }

    public Voittajat haeVoittajaJaHaastaja(List<Tulos> tulokset) {
        record Sarjat(Keilaaja keilaaja, int paras, int huonompi) {
        }

        List<Sarjat> sarjat = tulokset.stream()
                .map(t -> new Sarjat(t.getKeilaaja(), Math.max(t.getSarja1(), t.getSarja2()),
                        Math.min(t.getSarja1(), t.getSarja2())))
                .sorted((s1, s2) -> {
                    int vertaaParas = Integer.compare(s2.paras, s1.paras);
                    if (vertaaParas != 0)
                        return vertaaParas;
                    return Integer.compare(s2.huonompi, s1.huonompi);
                })
                .toList();

        Sarjat eka = sarjat.get(0);
        Sarjat toka = sarjat.size() > 1 ? sarjat.get(1) : null;

        // Tähän tulee myöhemmin logiikka joka valitsee voittajan jos tasa-peli
        if (toka != null && eka.paras == toka.paras && eka.huonompi == toka.huonompi) {
            throw new IllegalStateException("Tasapeli: käyttäjän täytyy valita voittaja käsin.");
        }

        return new Voittajat(eka.keilaaja, toka != null ? toka.keilaaja : null);
    }

    // Tallennetaan kaksintaistelu tietokantaan ja lisätään se listaan
    private void lisaaKaksintaistelu(GP gp, Keilaaja voittaja, Keilaaja haastaja,
            boolean vyoUnohtui, boolean hallitsevaPaikalla, boolean haastajaPaikalla) {
        KuppiksenKunkku kk = new KuppiksenKunkku();
        kk.setGp(gp);
        kk.setHallitseva(voittaja);
        kk.setHaastaja(haastaja);
        kk.setVyoUnohtui(vyoUnohtui);
        kk.setHallitsevaPaikalla(hallitsevaPaikalla);
        kk.setHaasajaPaikalla(haastajaPaikalla);
        kuppiksenKunkkuRepository.save(kk);
        kaudenKaksintaistelut.add(kk);
    }

    // Muodostetaan haastajalista, joka sisältää keilaajat ja heidän parhaat sarjat
    private List<Keilaaja> muodostaHaastajalista(List<Tulos> tulokset, Keilaaja poisjattava) {
        return tulokset.stream()
                .map(t -> Map.entry(t.getKeilaaja(), Math.max(t.getSarja1(), t.getSarja2())))
                .filter(e -> !e.getKey().equals(poisjattava))
                .sorted((e1, e2) -> {
                    int vertailu = e2.getValue().compareTo(e1.getValue());
                    if (vertailu != 0)
                        return vertailu;
                    int h1 = haeHuonompiSarja(e1.getKey(), tulokset);
                    int h2 = haeHuonompiSarja(e2.getKey(), tulokset);
                    return Integer.compare(h2, h1);
                })
                .map(Map.Entry::getKey)
                .toList();
    }

    // Haetaan keilaajan huonoin sarja, jotta voidaan verrata haastajalistan
    // järjestystä
    private int haeHuonompiSarja(Keilaaja k, List<Tulos> tulokset) {
        return tulokset.stream()
                .filter(t -> t.getKeilaaja().equals(k))
                .mapToInt(t -> Math.min(t.getSarja1(), t.getSarja2()))
                .findFirst().orElse(0);
    }

    /**
     * Valitsee voittajan kahden keilaajan välillä tietyn GP:n tulosten perusteella.
     * - Ensin verrataan parempia sarjoja (suurempi voittaa)
     * - Jos tasan, verrataan huonompia sarjoja (suurempi voittaa)
     * - Jos edelleen tasan, heitetään virhe: käyttäjän täytyy valita voittaja
     * manuaalisesti
     */
    private Keilaaja valitseVoittaja(GP gp, Keilaaja k1, Keilaaja k2) {
        Tulos t1 = tulosRepository.findByGpAndKeilaaja(gp, k1);
        Tulos t2 = tulosRepository.findByGpAndKeilaaja(gp, k2);

        int paras1 = Math.max(t1.getSarja1(), t1.getSarja2());
        int paras2 = Math.max(t2.getSarja1(), t2.getSarja2());

        if (paras1 > paras2)
            return k1;
        if (paras2 > paras1)
            return k2;

        int huono1 = Math.min(t1.getSarja1(), t1.getSarja2());
        int huono2 = Math.min(t2.getSarja1(), t2.getSarja2());

        if (huono1 > huono2)
            return k1;
        if (huono2 > huono1)
            return k2;

        throw new IllegalStateException("Tasapeli: käyttäjän täytyy valita voittaja käsin.");
    }

    /**
     * Palauttaa kaikki kuluvan kauden KuppiksenKunkku-kaksintaistelut.
     * Lista täyttyy jokaisen GP:n yhteydessä, kun kaksintaistelu tallennetaan.
     */
    public List<KuppiksenKunkku> getKaudenKaksintaistelut() {
        return kaudenKaksintaistelut;
    }

    /**
     * Palauttaa tietyn GP:n haastajalistan.
     * Lista muodostetaan jokaiselle GP:lle kisojen yhteydessä ja käytetään esim.
     * Thymeleaf-näytöillä.
     */
    public List<Keilaaja> getHaastajalista(GP gp) {
        return haastajalistat.getOrDefault(gp, Collections.emptyList());
    }

    /**
     * Record-luokka edustamaan voittaja–haastaja-paria.
     * Käytetään ensimmäisen GP:n käsittelyssä, jossa molemmat valitaan suoraan
     * tuloksista.
     */
    public record Voittajat(Keilaaja voittaja, Keilaaja haastaja) {
    }
}

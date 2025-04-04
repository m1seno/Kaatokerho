package k25.kaatokerho.service;

import k25.kaatokerho.domain.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import java.util.*;

//Vastaa 
@Service
public class KuppiksenKunkkuService {

    private final KuppiksenKunkkuRepository kuppiksenKunkkuRepository;
    private final TulosRepository tulosRepository;

    @Autowired
    public KuppiksenKunkkuService(KuppiksenKunkkuRepository kuppiksenKunkkuRepository, TulosRepository tulosRepository) {
        this.kuppiksenKunkkuRepository = kuppiksenKunkkuRepository;
        this.tulosRepository = tulosRepository;
    }

    private final List<KuppiksenKunkku> kaudenKaksintaistelut = new ArrayList<>();
    private final Map<GP, List<Keilaaja>> haastajalistat = new HashMap<>();

    public void kasitteleKuppiksenKunkku(GP gp, KuppiksenKunkku edellinen) {
        List<Tulos> tulokset = gp.getTulokset().stream()
                .filter(Tulos::getOsallistui)
                .toList();

        if (gp.getJarjestysnumero() == 1 || edellinen == null) {
            Voittajat v = haeVoittajaJaHaastaja(tulokset);
            lisaaKaksintaistelu(gp, v.voittaja(), v.haastaja());
            return;
        }

        Keilaaja edellinenVoittaja = edellinen.getHallitseva();
        List<Keilaaja> haastajat = muodostaHaastajalista(tulokset, edellinenVoittaja);

        boolean haltijaLokal = tulokset.stream()
                .anyMatch(t -> t.getKeilaaja().equals(edellinenVoittaja));

        Keilaaja uusiVoittaja;
        Keilaaja haastaja = null;

        if (haltijaLokal && !haastajat.isEmpty()) {
            haastaja = haastajat.get(0);
            uusiVoittaja = valitseVoittaja(gp, edellinenVoittaja, haastaja);
        } else {
            uusiVoittaja = haastajat.get(0);
        }

        lisaaKaksintaistelu(gp, uusiVoittaja, haastaja);
    }

    public Voittajat haeVoittajaJaHaastaja(List<Tulos> tulokset) {
        record Sarjat(Keilaaja keilaaja, int paras, int huonompi) {}

        List<Sarjat> sarjat = tulokset.stream()
                .map(t -> new Sarjat(t.getKeilaaja(), Math.max(t.getSarja1(), t.getSarja2()), Math.min(t.getSarja1(), t.getSarja2())))
                .sorted((s1, s2) -> {
                    int vertaaParas = Integer.compare(s2.paras, s1.paras);
                    if (vertaaParas != 0) return vertaaParas;
                    return Integer.compare(s2.huonompi, s1.huonompi);
                })
                .toList();

        Sarjat eka = sarjat.get(0);
        Sarjat toka = sarjat.size() > 1 ? sarjat.get(1) : null;

        //Tähän tulee myöhemmin logiikka joka valitsee voittajan jos tasa-peli
        if (toka != null && eka.paras == toka.paras && eka.huonompi == toka.huonompi) {
            throw new IllegalStateException("Tasapeli: käyttäjän täytyy valita voittaja käsin.");
        }
        

        return new Voittajat(eka.keilaaja, toka != null ? toka.keilaaja : null);
    }

    private void lisaaKaksintaistelu(GP gp, Keilaaja voittaja, Keilaaja haastaja) {
        KuppiksenKunkku kk = new KuppiksenKunkku();
        kk.setGp(gp);
        kk.setHallitseva(voittaja);
        kk.setHaastaja(haastaja);
        kuppiksenKunkkuRepository.save(kk);
        kaudenKaksintaistelut.add(kk);
    }

    private List<Keilaaja> muodostaHaastajalista(List<Tulos> tulokset, Keilaaja poisjattava) {
        return tulokset.stream()
                .map(t -> Map.entry(t.getKeilaaja(), Math.max(t.getSarja1(), t.getSarja2())))
                .filter(e -> !e.getKey().equals(poisjattava))
                .sorted((e1, e2) -> {
                    int vertailu = e2.getValue().compareTo(e1.getValue());
                    if (vertailu != 0) return vertailu;
                    int h1 = haeHuonompiSarja(e1.getKey(), tulokset);
                    int h2 = haeHuonompiSarja(e2.getKey(), tulokset);
                    return Integer.compare(h2, h1);
                })
                .map(Map.Entry::getKey)
                .toList();
    }

    private int haeHuonompiSarja(Keilaaja k, List<Tulos> tulokset) {
        return tulokset.stream()
                .filter(t -> t.getKeilaaja().equals(k))
                .mapToInt(t -> Math.min(t.getSarja1(), t.getSarja2()))
                .findFirst().orElse(0);
    }

    private Keilaaja valitseVoittaja(GP gp, Keilaaja k1, Keilaaja k2) {
        Tulos t1 = tulosRepository.findByGpAndKeilaaja(gp, k1);
        Tulos t2 = tulosRepository.findByGpAndKeilaaja(gp, k2);

        int paras1 = Math.max(t1.getSarja1(), t1.getSarja2());
        int paras2 = Math.max(t2.getSarja1(), t2.getSarja2());

        if (paras1 > paras2) return k1;
        if (paras2 > paras1) return k2;

        int huono1 = Math.min(t1.getSarja1(), t1.getSarja2());
        int huono2 = Math.min(t2.getSarja1(), t2.getSarja2());

        if (huono1 > huono2) return k1;
        if (huono2 > huono1) return k2;

        throw new IllegalStateException("Tasapeli: käyttäjän täytyy valita voittaja käsin.");
    }

    public List<KuppiksenKunkku> getKaudenKaksintaistelut() {
        return kaudenKaksintaistelut;
    }

    public List<Keilaaja> getHaastajalista(GP gp) {
        return haastajalistat.getOrDefault(gp, Collections.emptyList());
    }

    public record Voittajat(Keilaaja voittaja, Keilaaja haastaja) {}
}

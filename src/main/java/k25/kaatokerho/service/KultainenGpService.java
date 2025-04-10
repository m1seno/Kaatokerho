package k25.kaatokerho.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import org.springframework.stereotype.Service;

import k25.kaatokerho.domain.GP;
import k25.kaatokerho.domain.Kausi;
import k25.kaatokerho.domain.Keilaaja;
import k25.kaatokerho.domain.KeilaajaKausi;
import k25.kaatokerho.domain.KeilaajaKausiRepository;
import k25.kaatokerho.domain.KultainenGp;
import k25.kaatokerho.domain.KultainenGpRepository;
import k25.kaatokerho.domain.Tulos;

@Service
public class KultainenGpService {

    private final KultainenGpRepository kultainenGpRepository;
    private final KeilaajaKausiRepository keilaajaKausiRepository;

    public KultainenGpService(KultainenGpRepository kultainenGpRepository,
            KeilaajaKausiRepository keilaajaKausiRepository) {
        this.kultainenGpRepository = kultainenGpRepository;
        this.keilaajaKausiRepository = keilaajaKausiRepository;
    }

    // Säilytetään keilaajan hankkimia lisäpisteitä metodien ulkopuolella
    private final Map<Long, Double> keilaajaPisteetMap = new HashMap<>();

    public void kultainenPistelasku(GP gp) {
        if (!gp.isOnKultainenGp())
            return;

        Kausi kausi = gp.getKausi();

        // Käydään läpi kaikki kultaisen gp:n tulokset
        for (Tulos tulos : gp.getTulokset()) {

            // Skipataan jos ei osallistunut
            if (!tulos.getOsallistui())
                continue;

            Keilaaja keilaaja = tulos.getKeilaaja();
            Integer sarja1 = tulos.getSarja1();
            Integer sarja2 = tulos.getSarja2();

            // Lasketaan paras ja huonoin sarja
            int paras = Math.max(sarja1, sarja2);
            int huonoin = Math.min(sarja1, sarja2);

            // Kutsutaan metodia, joka laskee kultaisen gp:n parhaimman ja huonoimman sarjan
            gpParasJaHuonoin(gp, keilaaja, paras, huonoin);

            Optional<KeilaajaKausi> keilaajaKausiOpt = keilaajaKausiRepository.findByKeilaajaAndKausi(keilaaja, kausi);
            if (keilaajaKausiOpt.isEmpty())
                continue;

            // Haetaan paras ja huonoin voimassaoleva sarja ennen tätä GP:tä
            KeilaajaKausi keilaajaKausi = keilaajaKausiOpt.get();
            Integer kaudenParas = keilaajaKausi.getParasSarja();
            Integer kaudenHuonoin = keilaajaKausi.getHuonoinSarja();

            // Jos sivutaan tai parannetaan/huononnetaan, lisätään tai vähennetään piste.
            if (kaudenParas != null && paras >= kaudenParas) {
                paivitaKeilaajanKultainenPiste(keilaaja, 1);
            }
            if (kaudenHuonoin != null && huonoin <= kaudenHuonoin) {
                paivitaKeilaajanKultainenPiste(keilaaja, -1);
            }
        }
    }

    private void gpParasJaHuonoin(GP gp, Keilaaja keilaaja, int omaParas, int omaHuonoin) {
        List<Tulos> tulokset = gp.getTulokset();

        // Filteröidään streamista vain ne tulokset, jotka eivät ole keilaajan omaia tuloksia
        List<Tulos> muidenTulokset = tulokset.stream()
                .filter(t -> !t.getKeilaaja().getKeilaajaId().equals(keilaaja.getKeilaajaId()))
                .toList();

        // Lasketaan paras ja huonoin sarja kaikista muista keilaajista
        int parasKaikista = muidenTulokset.stream()
                .flatMap(t -> Stream.of(t.getSarja1(), t.getSarja2()))
                .filter(Objects::nonNull)
                .max(Integer::compareTo)
                .orElse(omaParas);

        int huonoinKaikista = muidenTulokset.stream()
                .flatMap(t -> Stream.of(t.getSarja1(), t.getSarja2()))
                .filter(Objects::nonNull)
                .min(Integer::compareTo)
                .orElse(omaHuonoin);

        // Lasketaan kuinka monta keilaajaa on saanut parhaan ja huonoimman sarjan
        long parhaat = muidenTulokset.stream()
                .flatMap(t -> Stream.of(t.getSarja1(), t.getSarja2()))
                .filter(Objects::nonNull)
                .filter(s -> s == parasKaikista)
                .count();

        long huonoimmat = muidenTulokset.stream()
                .flatMap(t -> Stream.of(t.getSarja1(), t.getSarja2()))
                .filter(Objects::nonNull)
                .filter(s -> s == huonoinKaikista)
                .count();

        /* 
         *  Vertaillaan omaa parasta ja huonointa sarjaa muiden tuloksiin.
         *  Jos tulos jaetaan, jaetaan myös lisä tai miinuspiste.
         */ 
        if (omaParas >= parasKaikista && parhaat > 0) {
            paivitaKeilaajanKultainenPiste(keilaaja, 1.0 / parhaat);
        } else if (omaParas > parasKaikista) {
            paivitaKeilaajanKultainenPiste(keilaaja, 1);
        }

        if (omaHuonoin <= huonoinKaikista && huonoimmat > 0) {
            paivitaKeilaajanKultainenPiste(keilaaja, -1.0 / huonoimmat);
        } else if (omaHuonoin < huonoinKaikista) {
            paivitaKeilaajanKultainenPiste(keilaaja, -1);
        }

        // Kutsutaan metodia, joka tallentaa kultaisen gp:n tulokset
        tallennaPisteet(gp);
    }


    // Lasketaan yhteen keilaajan lisäpisteet
    // ChatGPT:n ehdotus: käytetään merge-metodia, joka yhdistää arvot ja laskee yhteen
    private void paivitaKeilaajanKultainenPiste(Keilaaja keilaaja, double pisteet) {
        keilaajaPisteetMap.merge(keilaaja.getKeilaajaId(), pisteet, Double::sum);
    }

    private void tallennaPisteet(GP gp) {
        Kausi kausi = gp.getKausi();
        for (Tulos tulos : gp.getTulokset()) {
            Keilaaja keilaaja = tulos.getKeilaaja();
            double pisteet = keilaajaPisteetMap.getOrDefault(keilaaja.getKeilaajaId(), 0.0);

            KultainenGp kultainenGp = new KultainenGp();
            kultainenGp.setGp(gp);
            kultainenGp.setKeilaaja(keilaaja);
            kultainenGp.setLisapisteet(pisteet);
            kultainenGpRepository.save(kultainenGp);

            // Tallennetaan pisteet myös sarjataulukkoon
            Optional<KeilaajaKausi> keilaajaKausiOpt = keilaajaKausiRepository.findByKeilaajaAndKausi(keilaaja, kausi);
            if (keilaajaKausiOpt.isPresent()) {
                KeilaajaKausi keilaajaKausi = keilaajaKausiOpt.get();
                keilaajaKausi.setKaudenPisteet(keilaajaKausi.getKaudenPisteet() + pisteet);
                keilaajaKausiRepository.save(keilaajaKausi);
            }
        }

        // Tyhjennetään keilaajaPisteetMap, jotta se ei sisällä vanhoja arvoja seuraavassa GP:ssä
        keilaajaPisteetMap.clear();
    }
}
package k25.kaatokerho.service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Service;

import k25.kaatokerho.domain.GP;
import k25.kaatokerho.domain.GpRepository;
import k25.kaatokerho.domain.Kausi;
import k25.kaatokerho.domain.Keilaaja;
import k25.kaatokerho.domain.KeilaajaKausi;
import k25.kaatokerho.domain.KeilaajaKausiRepository;
import k25.kaatokerho.domain.Tulos;

@Service
public class KeilaajaKausiService {

    private final KeilaajaKausiRepository keilaajaKausiRepository;
    private final PistelaskuService pistelaskuService;
    private final KultainenGpService kultainenGpService;
    private final GpRepository gpRepository;

    public KeilaajaKausiService(
            KeilaajaKausiRepository keilaajaKausiRepository,
            PistelaskuService pistelaskuService,
            KultainenGpService kultainenGpService,
            GpRepository gpRepository) {
        this.keilaajaKausiRepository = keilaajaKausiRepository;
        this.pistelaskuService = pistelaskuService;
        this.kultainenGpService = kultainenGpService;
        this.gpRepository = gpRepository;
    }

    public void paivitaKeilaajaKausi(GP gp) {
        // Kutsutaan pistelaskua
        Map<Long, Double> tuloslista = pistelaskuService.laskeSijoitus(gp);

        // Kutsutaan KultainenGpServiceä
        kultainenGpService.kultainenPistelasku(gp);

        Kausi kausi = gp.getKausi();

        // Selvitetään GP:n voittajat
        double parasPiste = tuloslista.values().stream().mapToDouble(Double::doubleValue).max().orElse(0);

        // Haetaan tulosten tiedot
        List<Tulos> tulokset = gp.getTulokset();
        for (Tulos tulos : tulokset) {
            Integer sarja1 = tulos.getSarja1();
            Integer sarja2 = tulos.getSarja2();
            Integer gpParas = sarja1 != null && sarja2 != null ? Math.max(sarja1, sarja2) : null;
            Integer gpHuonoin = sarja1 != null && sarja2 != null ? Math.min(sarja1, sarja2) : null;
            Keilaaja keilaaja = tulos.getKeilaaja();
            Long keilaajaId = keilaaja.getKeilaajaId();
            boolean osallistui = tulos.getOsallistui();
            Double gpPisteet = osallistui ? tuloslista.getOrDefault(keilaajaId, 0.0) : 0.0;

            boolean voittaja = osallistui && tuloslista.containsKey(keilaajaId)
                    && tuloslista.get(keilaajaId) == parasPiste;

            // Tarkista, onko keilaaja jo olemassa kaudella
            Optional<KeilaajaKausi> keilaajaKausiOpt = keilaajaKausiRepository
                    .findByKeilaajaAndKausi(keilaaja, kausi);
            if (keilaajaKausiOpt.isPresent()) {
                // Haetaan vanhat tiedot
                KeilaajaKausi keilaajaKausi = keilaajaKausiOpt.get();
                Double vanhatPisteet = keilaajaKausi.getKaudenPisteet();
                Integer vanhatVoitot = keilaajaKausi.getVoittoja();
                Integer vanhatOsallistumiset = keilaajaKausi.getOsallistumisia();

                // Lasketaan uudet tiedot
                Double uudetPisteet = vanhatPisteet + gpPisteet;
                Integer uudetVoitot = voittaja ? vanhatVoitot + 1 : vanhatVoitot;
                Integer uudetOsallistumiset = osallistui ? vanhatOsallistumiset + 1 : vanhatOsallistumiset;

                // Päivitetään olemassaolevan keilaajakauden tietoja
                if (gpParas != null) {
                    Integer nykyinenParas = keilaajaKausi.getParasSarja();
                    keilaajaKausi.setParasSarja(
                            (nykyinenParas == null) ? gpParas : Math.max(nykyinenParas, gpParas));
                }
                if (gpHuonoin != null) {
                    Integer nykyinenHuonoin = keilaajaKausi.getHuonoinSarja();
                    keilaajaKausi.setHuonoinSarja(
                            (nykyinenHuonoin == null) ? gpHuonoin : Math.min(nykyinenHuonoin, gpHuonoin));
                }
                keilaajaKausi.setKaudenPisteet(uudetPisteet);
                keilaajaKausi.setVoittoja(uudetVoitot);
                keilaajaKausi.setOsallistumisia(uudetOsallistumiset);

                keilaajaKausiRepository.save(keilaajaKausi);
            } else {
                // Luo uusi keilaajakausi
                Integer voittoja = voittaja ? 1 : 0;
                Integer osallistumisia = osallistui ? 1 : 0;
                Double pisteet = gpPisteet;

                KeilaajaKausi uusiKeilaajaKausi = new KeilaajaKausi();
                uusiKeilaajaKausi.setKeilaaja(keilaaja);
                uusiKeilaajaKausi.setKausi(kausi);
                uusiKeilaajaKausi.setParasSarja(gpParas);
                uusiKeilaajaKausi.setHuonoinSarja(gpHuonoin);
                uusiKeilaajaKausi.setKaudenPisteet(pisteet);
                uusiKeilaajaKausi.setVoittoja(voittoja);
                uusiKeilaajaKausi.setOsallistumisia(osallistumisia);

                keilaajaKausiRepository.save(uusiKeilaajaKausi);
            }
        }
    }

    public void paivitaKaikkiKeilaajaKausiTiedot() {
        List<GP> kaikkiGp = (List<GP>) gpRepository.findAll();
    
        // Poistetaan kaikki keilaajakaudet
        keilaajaKausiRepository.deleteAll();
    
        // Luodaan uudet tilastot pohjautuen jäljellä oleviin GP:ihin
        for (GP gp : kaikkiGp) {
            paivitaKeilaajaKausi(gp);
        }
    }
}

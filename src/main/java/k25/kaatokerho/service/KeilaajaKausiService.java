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
                    && Double.compare(tuloslista.get(keilaajaId), parasPiste) == 0;

            // Tarkista, onko keilaaja jo olemassa kaudella
            KeilaajaKausi keilaajaKausi = haeTaiLuoKeilaajaKausi(keilaaja, kausi);

            // Päivitä paras sarja
            if (gpParas != null) {
                Integer nykyinen = keilaajaKausi.getParasSarja();
                keilaajaKausi.setParasSarja(nykyinen == null ? gpParas : Math.max(nykyinen, gpParas));
            }

            // Päivitä huonoin sarja
            if (gpHuonoin != null) {
                Integer nykyinen = keilaajaKausi.getHuonoinSarja();
                keilaajaKausi.setHuonoinSarja(nykyinen == null ? gpHuonoin : Math.min(nykyinen, gpHuonoin));
            }

            // Päivitä pisteet, voitot ja osallistumiset
            keilaajaKausi.setKaudenPisteet(keilaajaKausi.getKaudenPisteet() + gpPisteet);
            if (voittaja) {
                keilaajaKausi.setVoittoja(keilaajaKausi.getVoittoja() + 1);
            }
            if (osallistui) {
                keilaajaKausi.setOsallistumisia(keilaajaKausi.getOsallistumisia() + 1);
            }

            keilaajaKausiRepository.save(keilaajaKausi);

        }
    }

    public KeilaajaKausi haeTaiLuoKeilaajaKausi(Keilaaja keilaaja, Kausi kausi) {
        return keilaajaKausiRepository
                .findByKeilaajaAndKausi(keilaaja, kausi)
                .orElseGet(() -> {
                    KeilaajaKausi uusi = new KeilaajaKausi();
                    uusi.setKeilaaja(keilaaja);
                    uusi.setKausi(kausi);
                    uusi.setKaudenPisteet(0.0);
                    uusi.setVoittoja(0);
                    uusi.setOsallistumisia(0);
                    uusi.setParasSarja(null);
                    uusi.setHuonoinSarja(null);
                    return uusi;
                });
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

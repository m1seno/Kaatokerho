package k25.kaatokerho.service;

import org.springframework.stereotype.Service;

import k25.kaatokerho.domain.GP;
import k25.kaatokerho.domain.GpRepository;
import k25.kaatokerho.domain.Kausi;
import k25.kaatokerho.domain.KausiRepository;
import k25.kaatokerho.domain.Keilahalli;
import k25.kaatokerho.domain.KeilahalliRepository;
import k25.kaatokerho.domain.dto.UusiGpDTO;

@Service
public class LisaaGpService {

    private final GpRepository gpRepository;
    private final KausiRepository kausiRepository;
    private final KeilahalliRepository keilahalliRepository;

    public LisaaGpService(GpRepository gpRepository, KausiRepository kausiRepository, KeilahalliRepository keilahalliRepository) {
        this.gpRepository = gpRepository;
        this.kausiRepository = kausiRepository;
        this.keilahalliRepository = keilahalliRepository;
    }

    public UusiGpDTO luoUusiGp() {
        // Haetaan aktiivinen kausi, joka on viimeisin kausi tietokannassa
        Kausi aktiivinenKausi = kausiRepository.findTopByOrderByKausiIdDesc();
        if (aktiivinenKausi == null) {
            throw new IllegalStateException("Ei aktiivista kautta.");
        }
        // Tarkistetaan montako GP:tä on jo lisätty kauteen ja lasketaan seuraava järjestysnumero
        int seuraavaJarjestysnumero = gpRepository.countByKausi(aktiivinenKausi) + 1;

        // Tarkistetaan, että kauden suunniteltu GP-määrä ei ylity
        if (seuraavaJarjestysnumero > aktiivinenKausi.getSuunniteltuGpMaara()) {
            throw new IllegalStateException("Kaikki kauden GP:t on jo keilattu.");
        }

        // Luodaan uusi GP-olio ja palautetaan se Controllerille
        UusiGpDTO gp = new UusiGpDTO();
        gp.setJarjestysnumero(seuraavaJarjestysnumero);
        gp.setKausi(aktiivinenKausi);
        return gp;
    }

    public void tallennaGp(UusiGpDTO uusiGp) {
        Kausi kausi = kausiRepository.findTopByOrderByKausiIdDesc();
        Keilahalli halli = keilahalliRepository.findById(uusiGp.getKeilahalliId())
                .orElseThrow(() -> new IllegalArgumentException("Keilahallia ei löytynyt"));

        // Luodaan uusi Gp-olio ja asetetaan sen tiedot
        GP gp = new GP();
        gp.setJarjestysnumero(uusiGp.getJarjestysnumero());
        gp.setPvm(uusiGp.getPvm());
        gp.setKeilahalli(halli);
        gp.setOnKultainenGp(uusiGp.isKultainenGp());
        gp.setKausi(kausi);

        gpRepository.save(gp);

        kausi.setGpMaara(kausi.getGpMaara() + 1);
        kausiRepository.save(kausi);
    }
}

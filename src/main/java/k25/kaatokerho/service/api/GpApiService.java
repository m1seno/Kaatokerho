package k25.kaatokerho.service.api;

import org.springframework.stereotype.Service;

import k25.kaatokerho.domain.GP;
import k25.kaatokerho.domain.GpRepository;
import k25.kaatokerho.domain.Kausi;
import k25.kaatokerho.domain.KausiRepository;
import k25.kaatokerho.domain.Keilahalli;
import k25.kaatokerho.domain.KeilahalliRepository;
import k25.kaatokerho.domain.dto.UusiGpDTO;

@Service
public class GpApiService {

    private final GpRepository gpRepository;
    private final KausiRepository kausiRepository;
    private final KeilahalliRepository keilahalliRepository;

    public GpApiService(GpRepository gpRepository, KausiRepository kausiRepository,
            KeilahalliRepository keilahalliRepository) {
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
        // Tarkistetaan montako GP:tä on jo lisätty kauteen ja lasketaan seuraava
        // järjestysnumero
        int seuraavaJarjestysnumero = gpRepository.countByKausi(aktiivinenKausi) + 1;

        // Tarkistetaan, että kauden suunniteltu GP-määrä ei ylity
        if (seuraavaJarjestysnumero > aktiivinenKausi.getSuunniteltuGpMaara()) {
            throw new IllegalStateException("Kaikki kauden GP:t on jo keilattu.");
        }

        // Luodaan uusi GP-olio ja palautetaan se Controllerille
        UusiGpDTO gp = new UusiGpDTO();
        gp.setJarjestysnumero(seuraavaJarjestysnumero);
        gp.setKausiId(aktiivinenKausi.getKausiId());
        return gp;
    }

    public void tallennaGp(UusiGpDTO uusiGp) {
        Kausi kausi = kausiRepository.findTopByOrderByKausiIdDesc();
        if (kausi == null)
            throw new IllegalStateException("Ei aktiivista kautta.");

        // laske järjestysnumero aina palvelussa
        int seuraava = gpRepository.countByKausi(kausi) + 1;
        if (seuraava > kausi.getSuunniteltuGpMaara()) {
            throw new IllegalStateException("Kaudelle ei voi lisätä enempää GP:itä.");
        }

        Keilahalli halli = keilahalliRepository.findById(uusiGp.getKeilahalliId())
                .orElseThrow(() -> new IllegalArgumentException("Keilahallia ei löytynyt"));

        boolean onKultainen = Boolean.TRUE.equals(uusiGp.isKultainenGp());

        // Kultaisen GP:n rajoitustarkistus
        if (onKultainen) {
            int kultaisia = gpRepository.countByKausiAndOnKultainenGpTrue(kausi);
            int maksimi = 2; // voit muuttaa tarpeen mukaan
            if (kultaisia >= maksimi) {
                throw new IllegalStateException("Kaudelle ei voi lisätä enempää kultaisia GP:itä (" + maksimi + ").");
            }
        }

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

    public GP tallennaGpJaPalauta(UusiGpDTO uusiGp) {
        // Haetaan viimeisin kausi
        Kausi kausi = kausiRepository.findTopByOrderByKausiIdDesc();

        // Estetään GP:n lisääminen jos kauden gpMaara on jo täynnä
        if (kausi.getGpMaara() >= kausi.getSuunniteltuGpMaara()) {
            throw new IllegalStateException("Kaudelle ei voi lisätä enempää GP:itä.");
        }

        // Haetaan keilahalli ID:n perusteella
        Keilahalli halli = keilahalliRepository.findById(uusiGp.getKeilahalliId())
                .orElseThrow(() -> new IllegalArgumentException("Keilahallia ei löytynyt"));

        // Luodaan GP-olio ja asetetaan tiedot
        GP gp = new GP();
        gp.setJarjestysnumero(uusiGp.getJarjestysnumero());
        gp.setPvm(uusiGp.getPvm());
        gp.setKeilahalli(halli);
        gp.setOnKultainenGp(uusiGp.isKultainenGp());
        gp.setKausi(kausi);

        // Tallennetaan GP
        GP tallennettuGp = gpRepository.save(gp);

        // Päivitetään kauden gpMaara
        kausi.setGpMaara(kausi.getGpMaara() + 1);
        kausiRepository.save(kausi);

        return tallennettuGp;
    }

}

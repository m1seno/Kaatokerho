package k25.kaatokerho.service.api;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import jakarta.transaction.Transactional;
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

    @Transactional
    public List<GP> haeGpKausella(Long kausiId) {
        return gpRepository.findByKausi_KausiIdOrderByJarjestysnumeroAsc(kausiId);
    }

    @Transactional
    public List<GP> haeNykyisenKaudenGp() {
        Kausi aktiivinen = kausiRepository.findTopByOrderByKausiIdDesc();
        if (aktiivinen == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Ei aktiivista kautta.");
        }
        return gpRepository.findByKausi_KausiIdOrderByJarjestysnumeroAsc(aktiivinen.getKausiId());
    }

    @Transactional
    public GP tallennaGpJaPalauta(UusiGpDTO uusiGp) {
        // Haetaan viimeisin kausi
        Kausi kausi = kausiRepository.findTopByOrderByKausiIdDesc();

        // Estetään GP:n lisääminen jos kauden gpMaara on jo täynnä
        if (kausi.getGpMaara() >= kausi.getSuunniteltuGpMaara()) {
            throw new IllegalStateException("Kaudelle ei voi lisätä enempää GP:itä.");
        }

        Long keilahalliId = uusiGp.getKeilahalliId();
        if (keilahalliId == null) {
            throw new IllegalArgumentException("Keilahalli ID ei voi olla null");
        }

        // Haetaan keilahalli ID:n perusteella
        Keilahalli halli = keilahalliRepository.findById(keilahalliId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Keilahallia ei löytynyt"));

        boolean onKultainen = Boolean.TRUE.equals(uusiGp.isKultainenGp());
        if (onKultainen) {
            int kultaisia = gpRepository.countByKausiAndOnKultainenGpTrue(kausi);
            if (kultaisia >= 2) {
                throw new IllegalStateException("Kaudelle ei voi lisätä enempää kultaisia GP:itä (2).");
            }
        }

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

    @Transactional
    public GP paivitaKultaisuus(GP gp, boolean uusiArvo) {
        if (uusiArvo == Boolean.TRUE.equals(gp.isOnKultainenGp())) return gp; // ei muutosta

        if (uusiArvo) {
            int kultaisia = gpRepository.countByKausiAndOnKultainenGpTrue(gp.getKausi());
            if (kultaisia >= 2) {
                throw new IllegalStateException("Kaudelle ei voi asettaa enempää kultaisia GP:itä (2).");
            }
        }
        gp.setOnKultainenGp(uusiArvo);
        return gpRepository.save(gp);
    }

}

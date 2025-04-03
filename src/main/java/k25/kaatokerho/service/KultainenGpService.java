package k25.kaatokerho.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import k25.kaatokerho.domain.GP;
import k25.kaatokerho.domain.Kausi;
import k25.kaatokerho.domain.Keilaaja;
import k25.kaatokerho.domain.KeilaajaKausi;
import k25.kaatokerho.domain.KeilaajaKausiRepository;
import k25.kaatokerho.domain.KultainenGp;
import k25.kaatokerho.domain.KultainenGpRepository;

@Service
public class KultainenGpService {

    private final KultainenGpRepository kultainenGpRepository;
    private final KeilaajaKausiRepository keilaajaKausiRepository;

    public KultainenGpService(KultainenGpRepository kultainenGPRepository,
                               KeilaajaKausiRepository keilaajaKausiRepository) {
        this.kultainenGpRepository = kultainenGPRepository;
        this.keilaajaKausiRepository = keilaajaKausiRepository;
    }

    public void pistemuutokset(GP gp, Keilaaja keilaaja, int lisäpisteet) {
        KultainenGp uusi = new KultainenGp();
        uusi.setGp(gp);
        uusi.setKeilaaja(keilaaja);
        kultainenGpRepository.save(uusi);
    }

    public void kultainenPistelasku(boolean onKultainenGp, int sarja1, int sarja2, Keilaaja keilaaja, Kausi kausi, GP gp) {
        if (!onKultainenGp) return;

        int paras = Math.max(sarja1, sarja2);
        int huonoin = Math.min(sarja1, sarja2);

        // Hae keilaajan tilastot kyseiseltä kaudelta
        Optional<KeilaajaKausi> keilaajaKausi = keilaajaKausiRepository.findByKeilaajaAndKausi(keilaaja.getKeilaajaId(), kausi.getKausiId());
        if (keilaajaKausi.isEmpty()) {
            throw new IllegalArgumentException("KeilaajaKausi not found for keilaajaId: " + keilaaja.getKeilaajaId() + " and kausiId: " + kausi.getKausiId());
        }
        int kaudenParas = keilaajaKausi.get().getParasSarja() != null ? keilaajaKausi.get().getParasSarja() : 0;
        int kaudenHuonoin = keilaajaKausi.get().getHuonoinSarja() != null ? keilaajaKausi.get().getHuonoinSarja() : 0;

        // Lisäpiste kauden parhaasta, miinuspiste huonoimmasta
        if (paras >= kaudenParas) {
            pistemuutokset(gp, keilaaja, 1);
            
        } else if (huonoin <= kaudenHuonoin) {
            pistemuutokset(gp, keilaaja, -1);
        }

        // Lisäpiste gp:n parhaasta, miinuspiste huonoimmasta
        
    }
}

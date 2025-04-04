package k25.kaatokerho.service;

import java.util.List;
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

    public KultainenGpService(KultainenGpRepository kultainenGPRepository,
            KeilaajaKausiRepository keilaajaKausiRepository) {
        this.kultainenGpRepository = kultainenGPRepository;
        this.keilaajaKausiRepository = keilaajaKausiRepository;
    }

    public void kultainenPistelasku(boolean onKultainenGp, Integer sarja1, Integer sarja2, Keilaaja keilaaja, Kausi kausi,
            GP gp) {
        if (!onKultainenGp) return;

        // Jos sarjat ovat null, keilaaja ei osallistunut → ei pistelaskentaa
        if (sarja1 == null || sarja2 == null) return;

        int paras = Math.max(sarja1, sarja2);
        int huonoin = Math.min(sarja1, sarja2);

        // Hae keilaajan tilastot kyseiseltä kaudelta
        Optional<KeilaajaKausi> keilaajaKausi = keilaajaKausiRepository.findByKeilaajaAndKausi(keilaaja.getKeilaajaId(),
                kausi.getKausiId());
        if (keilaajaKausi.isEmpty()) {
            throw new IllegalArgumentException("KeilaajaKausi not found for keilaajaId: " + keilaaja.getKeilaajaId()
                    + " and kausiId: " + kausi.getKausiId());
        }
        Integer kaudenParas = keilaajaKausi.get().getParasSarja();
        Integer kaudenHuonoin = keilaajaKausi.get().getHuonoinSarja();

        // Lisäpiste gp:n parhaasta, miinuspiste huonoimmasta
        gpParasJaHuonoin(gp, keilaaja, paras, huonoin);

        // Jos KultainenGP on kauden ensimmäinen GP -> ei pistelisäykiä tai vähennyksiä.
        if (kaudenParas == null || kaudenHuonoin == null) {
            pistemuutokset(gp, keilaaja, 0);
            return;
        }
        // Lisäpiste kauden parhaasta, miinuspiste huonoimmasta
        if (paras >= kaudenParas) {
            pistemuutokset(gp, keilaaja, 1);

        } else if (huonoin <= kaudenHuonoin) {
            pistemuutokset(gp, keilaaja, -1);
        } else {
            pistemuutokset(gp, keilaaja, 0);
        }

    }

    // Vertaillaan keilaajan tulosta muiden keilaajien tuloksiin
    public void gpParasJaHuonoin(GP gp, Keilaaja keilaaja, int omaParas, int omaHuonoin) {
        List<Tulos> tulokset = gp.getTulokset();

        // Poistetaan keilaajan omat tulokset vertailusta
        List<Tulos> muidenTulokset = tulokset.stream()
                .filter(t -> !t.getKeilaaja().getKeilaajaId().equals(keilaaja.getKeilaajaId()))
                .toList();

        // Haetaan muiden paras ja huonoin sarjat
        int parasKaikista = muidenTulokset.stream()
                .flatMap(t -> Stream.of(t.getSarja1(), t.getSarja2()))
                .max(Integer::compareTo)
                .orElseThrow(() -> new IllegalStateException("GP:llä ei ole vertailutuloksia"));

        int huonoinKaikista = muidenTulokset.stream()
                .flatMap(t -> Stream.of(t.getSarja1(), t.getSarja2()))
                .min(Integer::compareTo)
                .orElseThrow(() -> new IllegalStateException("GP:llä ei ole vertailutuloksia"));

        // Kuinka moni keilaaja heitti parhaan/huonoimman sarjan?
        long parhaat = muidenTulokset.stream()
                .flatMap(t -> Stream.of(t.getSarja1(), t.getSarja2()))
                .filter(s -> s == parasKaikista)
                .count();

        long huonoimmat = muidenTulokset.stream()
                .flatMap(t -> Stream.of(t.getSarja1(), t.getSarja2()))
                .filter(s -> s == huonoinKaikista)
                .count();

        // Vertaillaan omaan tulokseen ja jaetaan piste jos kuuluu joukkoon
        if (omaParas == parasKaikista && parhaat > 0) {
            double piste = 1 / (int) parhaat;
            pistemuutokset(gp, keilaaja, piste);
        } else if (omaParas == parasKaikista) {
            pistemuutokset(gp, keilaaja, 1);
        } else {
            pistemuutokset(gp, keilaaja, 0);
        }

        if (omaHuonoin == huonoinKaikista && huonoimmat > 0) {
            double miinus = -1 / (int) huonoimmat;
            pistemuutokset(gp, keilaaja, miinus);
        } else if (omaParas == huonoinKaikista) {
            pistemuutokset(gp, keilaaja, -1);
        } else {
            pistemuutokset(gp, keilaaja, 0);
        }
    }

    public void pistemuutokset(GP gp, Keilaaja keilaaja, double lisapisteet) {
        KultainenGp uusi = new KultainenGp();
        uusi.setGp(gp);
        uusi.setKeilaaja(keilaaja);
        uusi.setLisapisteet(lisapisteet);
        kultainenGpRepository.save(uusi);
    }
}

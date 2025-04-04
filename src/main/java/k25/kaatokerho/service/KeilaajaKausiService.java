package k25.kaatokerho.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import k25.kaatokerho.domain.GP;
import k25.kaatokerho.domain.Kausi;
import k25.kaatokerho.domain.Keilaaja;
import k25.kaatokerho.domain.KeilaajaKausi;
import k25.kaatokerho.domain.KeilaajaKausiRepository;
import k25.kaatokerho.domain.Tulos;

@Service
public class KeilaajaKausiService {

    @Autowired
    private KeilaajaKausiRepository keilaajaKausiRepository;

    public void kasitteleKeilaajaKausi(GP gp) {
        List<Tulos> tulokset = gp.getTulokset();
        for (Tulos tulos : tulokset) {
            int sarja1 = tulos.getSarja1();
            int sarja2 = tulos.getSarja2();
            int gpParas = Math.max(sarja1, sarja2);
            int gpHuonoin = Math.min(sarja1, sarja2);
            Keilaaja keilaaja = tulos.getKeilaaja();
            Kausi kausi = gp.getKausi();

            // Tarkista, onko keilaaja jo olemassa kaudella
            Optional<KeilaajaKausi> keilaajaKausiOpt = keilaajaKausiRepository
                    .findByKeilaajaAndKausi(keilaaja.getKeilaajaId(), kausi.getKausiId());
            if (keilaajaKausiOpt.isPresent()) {
                // Päivitä olemassa olevaa keilaajakauden tietoa
                KeilaajaKausi keilaajaKausi = keilaajaKausiOpt.get();
                keilaajaKausi.setParasSarja(Math.max(keilaajaKausi.getParasSarja(), gpParas));
                keilaajaKausi.setHuonoinSarja(Math.min(keilaajaKausi.getHuonoinSarja(), gpHuonoin));
                keilaajaKausiRepository.save(keilaajaKausi);
            } else {
                // Luo uusi keilaajakausi
                KeilaajaKausi newKeilaajaKausi = new KeilaajaKausi(keilaaja, kausi, gpParas, gpHuonoin);
                keilaajaKausiRepository.save(newKeilaajaKausi);
            }
        }
    }
}

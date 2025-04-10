package k25.kaatokerho.service;

import org.springframework.stereotype.Service;

import k25.kaatokerho.domain.GP;
import k25.kaatokerho.domain.GpRepository;
import k25.kaatokerho.domain.Keilaaja;
import k25.kaatokerho.domain.KeilaajaRepository;
import k25.kaatokerho.domain.Tulos;
import k25.kaatokerho.domain.TulosRepository;
import k25.kaatokerho.domain.dto.LisaaTuloksetDTO;

@Service
public class TulosService {

    private final TulosRepository tulosRepository;
    private final KeilaajaRepository keilaajaRepository;
    private final GpRepository gpRepository;
    private final KeilaajaKausiService keilaajaKausiService;

    public TulosService(TulosRepository tulosRepository, KeilaajaRepository keilaajaRepository,
                        GpRepository gpRepository, KeilaajaKausiService keilaajaKausiService) {
        this.tulosRepository = tulosRepository;
        this.keilaajaRepository = keilaajaRepository;
        this.gpRepository = gpRepository;
        this.keilaajaKausiService = keilaajaKausiService;
    }

    public void tallennaTulokset(LisaaTuloksetDTO dto) {
        GP gp = gpRepository.findById(dto.getGpId())
                .orElseThrow(() -> new IllegalArgumentException("GP:tä ei löydy"));

        // Käydään läpi tulokset ja tallennetaan ne tietokantaan
        for (LisaaTuloksetDTO.TulosForm tf : dto.getTulokset()) {
            Keilaaja keilaaja = keilaajaRepository.findById(tf.getKeilaajaId())
                    .orElseThrow(() -> new IllegalArgumentException("Keilaajaa ei löydy"));

            Tulos tulos = new Tulos();
            tulos.setGp(gp);
            tulos.setKeilaaja(keilaaja);
            tulos.setSarja1(tf.getSarja1());
            tulos.setSarja2(tf.getSarja2());
            tulos.setOsallistui(tf.getSarja1() != null && tf.getSarja2() != null);

            tulosRepository.save(tulos);
        }

        // Päivitetään keilaajaKausi tiedot GP:n perusteella
        keilaajaKausiService.paivitaKeilaajaKausi(gp);
    }
}

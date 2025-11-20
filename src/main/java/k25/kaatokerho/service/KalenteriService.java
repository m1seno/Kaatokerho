package k25.kaatokerho.service;

import java.util.Comparator;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import k25.kaatokerho.domain.GP;
import k25.kaatokerho.domain.GpRepository;
import k25.kaatokerho.domain.Kausi;
import k25.kaatokerho.domain.KausiRepository;
import k25.kaatokerho.domain.Tulos;
import k25.kaatokerho.domain.dto.KalenteriDTO;
import k25.kaatokerho.exception.ApiException;

@Service
public class KalenteriService {

    private final GpRepository gpRepository;
    private final KausiRepository kausiRepository;

    public KalenteriService(GpRepository gpRepository, KausiRepository kausiRepository) {
        this.gpRepository = gpRepository;
        this.kausiRepository = kausiRepository;
    }

    /** Kuluvan kauden GP-kalenteri voittajineen. */
    public List<KalenteriDTO> gpTiedotNykyinenKausi() {
        Kausi kausi = kausiRepository.findTopByOrderByKausiIdDesc();
        if (kausi == null) {
            throw new ApiException(HttpStatus.NOT_FOUND, "Yhtään kautta ei ole vielä tallennettu");
        }

        List<GP> gpLista = gpRepository.findByKausi_KausiIdOrderByJarjestysnumeroAsc(kausi.getKausiId());

        return gpLista.stream().map(gp -> {
            List<Tulos> osallistujat = gp.getTulokset().stream()
                    .filter(t -> Boolean.TRUE.equals(t.getOsallistui()))
                    .toList();

            Tulos parasTulos = osallistujat.stream()
                    .max(Comparator.comparingInt(t -> t.getSarja1() + t.getSarja2()))
                    .orElse(null);

            String voittajaNimi = "-";
            Integer voittotulos = null;

            if (parasTulos != null) {
                voittajaNimi = parasTulos.getKeilaaja().getEtunimi() + " " +
                               parasTulos.getKeilaaja().getSukunimi();
                voittotulos = parasTulos.getSarja1() + parasTulos.getSarja2();
            }

            return new KalenteriDTO(
                    gp.getJarjestysnumero(),
                    gp.getPvm(),
                    gp.getKeilahalli().getNimi(),
                    voittajaNimi,
                    voittotulos
            );
        }).toList();
    }

    /** Voittotulosten keskiarvo annetulle listalle. */
    public double laskeKeskiarvo(List<KalenteriDTO> dtoLista) {
        return dtoLista.stream()
                .filter(dto -> dto.getVoittotulos() != null)
                .mapToInt(KalenteriDTO::getVoittotulos)
                .average()
                .orElse(0.0);
    }
}

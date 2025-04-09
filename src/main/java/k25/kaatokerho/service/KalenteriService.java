package k25.kaatokerho.service;

import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Service;

import k25.kaatokerho.domain.GP;
import k25.kaatokerho.domain.GpRepository;
import k25.kaatokerho.domain.Tulos;
import k25.kaatokerho.domain.dto.KalenteriDTO;

@Service
public class KalenteriService {

    private final GpRepository gpRepository;

    public KalenteriService(GpRepository gpRepository) {
        this.gpRepository = gpRepository;
    }

    public List<KalenteriDTO> GpTiedot() {
        List<GP> gpLista = (List<GP>) gpRepository.findAll();

        // Palauttaa GP-listan kutsujalle, jossa jokaiselle GP:lle on laskettu voittaja,
        // voittotulos ja keskiarvo
        return gpLista.stream().map(gp -> {
            List<Tulos> osallistujat = gp.getTulokset().stream()
                    .filter(Tulos::getOsallistui)
                    .toList();

            // Selvitetään voittaja ja voittotulos
            Tulos parasTulos = osallistujat.stream()
                    .max(Comparator.comparingInt(t -> t.getSarja1() + t.getSarja2()))
                    .orElse(null);

            String voittajaEtunimi = parasTulos != null ? parasTulos.getKeilaaja().getEtunimi() : "-";
            String voittajaSukunimi = parasTulos != null ? parasTulos.getKeilaaja().getSukunimi() : "-";
            String voittajaNimi = parasTulos != null ? voittajaEtunimi + " " + voittajaSukunimi : "-";
            Integer voittotulos = parasTulos != null ? (parasTulos.getSarja1() + parasTulos.getSarja2()) : null;

            // Kertoo mitä yksittäiselle GP:lle on tapahtunut
            return new KalenteriDTO(
                    gp.getJarjestysnumero(),
                    gp.getPvm(),
                    gp.getKeilahalli().getNimi(),
                    voittajaNimi,
                    voittotulos);
        }).toList();
    }

    public double laskeKeskiarvo(List<KalenteriDTO> dtoLista) {
        return dtoLista.stream()
                .filter(dto -> dto.getVoittotulos() != null)
                .mapToInt(KalenteriDTO::getVoittotulos)
                .average()
                .orElse(0.0);
    }
}

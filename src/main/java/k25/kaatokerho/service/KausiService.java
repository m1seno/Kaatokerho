package k25.kaatokerho.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import k25.kaatokerho.domain.Kausi;
import k25.kaatokerho.domain.KausiRepository;
import k25.kaatokerho.domain.dto.KausiDTO;
import k25.kaatokerho.domain.dto.UusiKausiDTO;

@Service
public class KausiService {

    private final KausiRepository kausiRepository;

    public KausiService(KausiRepository kausiRepository) {
        this.kausiRepository = kausiRepository;
    }

    private KausiDTO mapToDto(Kausi kausi) {
        return KausiDTO.builder()
                .nimi(kausi.getNimi())
                .gpMaara(kausi.getGpMaara())
                .suunniteltuGpMaara(kausi.getSuunniteltuGpMaara())
                .osallistujamaara(kausi.getOsallistujamaara())
                .build();
    }

    // Haetaan yhden kauden tiedot
    public KausiDTO getKausiById(Long kausiId) {
        Kausi kausi = kausiRepository.findById(kausiId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Kautta ei löydy id:llä: " + kausiId));

        return mapToDto(kausi);
    }

    // Haetaan kaikki kaudet
    public List<KausiDTO> getAllKausi() {
        return kausiRepository.findAll()
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    // Haetaan nykyinen kausi
    public KausiDTO getCurrentKausi() {
        Kausi kausi = kausiRepository.findTopByOrderByKausiIdDesc();

        if (kausi == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Yhtään kautta ei ole vielä tallennettu");
        }

        return mapToDto(kausi);
    }

    // Lisää uusi kausi
    public Kausi addNewKausi(UusiKausiDTO dto) {

        if (dto.getNimi() != null && kausiRepository.findByNimi(dto.getNimi()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Kausi " + dto.getNimi() + " on jo olemassa.");
        } else {
            Kausi kausi = new Kausi();
            kausi.setNimi(dto.getNimi());
            kausi.setGpMaara(0);
            kausi.setSuunniteltuGpMaara(dto.getSuunniteltuGpMaara());
            kausi.setOsallistujamaara(dto.getOsallistujamaara());
            Kausi tallennettuKausi = kausiRepository.save(kausi);

            return tallennettuKausi;

        }

    }
}

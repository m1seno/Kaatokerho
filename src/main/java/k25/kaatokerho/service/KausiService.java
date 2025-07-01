package k25.kaatokerho.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import k25.kaatokerho.domain.Kausi;
import k25.kaatokerho.domain.KausiRepository;
import k25.kaatokerho.domain.dto.KausiResponseDTO;
import k25.kaatokerho.domain.dto.UusiKausiDTO;
import k25.kaatokerho.exception.ApiException;

@Service
public class KausiService {

    private final KausiRepository kausiRepository;

    public KausiService(KausiRepository kausiRepository) {
        this.kausiRepository = kausiRepository;
    }

    private KausiResponseDTO mapToDto(Kausi kausi) {
        return KausiResponseDTO.builder()
                .nimi(kausi.getNimi())
                .gpMaara(kausi.getGpMaara())
                .suunniteltuGpMaara(kausi.getSuunniteltuGpMaara())
                .osallistujamaara(kausi.getOsallistujamaara())
                .build();
    }

    // Haetaan yhden kauden tiedot
    public KausiResponseDTO getKausiById(Long kausiId) {
        Kausi kausi = kausiRepository.findById(kausiId).orElse(null);
        if (kausi == null) {
            throw new ApiException(HttpStatus.NOT_FOUND, "Kautta ei löytynyt ID:llä " + kausiId);
        }

        return mapToDto(kausi);
    }

    // Haetaan kaikki kaudet
    public List<KausiResponseDTO> getAllKausi() {
        List <KausiResponseDTO> kausilista = kausiRepository.findAll()
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());

        if (kausilista.isEmpty()) {
            throw new ApiException(HttpStatus.NOT_FOUND, "Yhtään kautta ei ole vielä tallennettu");
        }
        return kausilista;
    }

    // Haetaan nykyinen kausi
    public KausiResponseDTO getCurrentKausi() {
        Kausi kausi = kausiRepository.findTopByOrderByKausiIdDesc();

        if (kausi == null) {
            throw new ApiException(HttpStatus.NOT_FOUND, "Yhtään kautta ei ole vielä tallennettu");
        }

        return mapToDto(kausi);
    }

    // Lisää uusi kausi
    public KausiResponseDTO addNewKausi(UusiKausiDTO dto) {

        String nimi = dto.getNimi().trim();

        if (nimi != null && kausiRepository.findByNimi(dto.getNimi()).isPresent()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Kausi " + nimi + " on jo olemassa.");
        } else {
            Kausi kausi = new Kausi();
            kausi.setNimi(nimi);
            kausi.setGpMaara(0);
            kausi.setSuunniteltuGpMaara(dto.getSuunniteltuGpMaara());
            kausi.setOsallistujamaara(dto.getOsallistujamaara());
            Kausi uusiKausi = kausiRepository.save(kausi);

            return mapToDto(uusiKausi);
        }

    }

    //Päivitä kausi
    public KausiResponseDTO updateKausi(Long kausiId, UusiKausiDTO dto) {
        Kausi kausi = kausiRepository.findById(kausiId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Kautta ei löytynyt ID:llä " + kausiId));

        String uusiNimi = dto.getNimi().trim();

        if (uusiNimi != null && !uusiNimi.equals(kausi.getNimi()) &&
            kausiRepository.findByNimi(uusiNimi).isPresent()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Kausi " + uusiNimi + " on jo olemassa.");
        }

        kausi.setNimi(uusiNimi);
        kausi.setSuunniteltuGpMaara(dto.getSuunniteltuGpMaara());
        kausi.setOsallistujamaara(dto.getOsallistujamaara());

        Kausi paivitettyKausi = kausiRepository.save(kausi);

        return mapToDto(paivitettyKausi);
    }

    // Poista kausi
    public void deleteKausi(Long kausiId) {
        Kausi kausi = kausiRepository.findById(kausiId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Kautta ei löytynyt ID:llä " + kausiId));

        kausiRepository.delete(kausi);
    }
}

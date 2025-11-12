package k25.kaatokerho.service.api;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import k25.kaatokerho.domain.Kausi;
import k25.kaatokerho.domain.KausiRepository;
import k25.kaatokerho.domain.dto.ResponseKausiDTO;
import k25.kaatokerho.domain.dto.UusiKausiDTO;
import k25.kaatokerho.exception.ApiException;

@Service
public class KausiApiService {

    private final KausiRepository kausiRepository;

    public KausiApiService(KausiRepository kausiRepository) {
        this.kausiRepository = kausiRepository;
    }

    private ResponseKausiDTO mapToDto(Kausi kausi) {
        return ResponseKausiDTO.builder()
                .kausiId(kausi.getKausiId())
                .nimi(kausi.getNimi())
                .gpMaara(kausi.getGpMaara())
                .suunniteltuGpMaara(kausi.getSuunniteltuGpMaara())
                .osallistujamaara(kausi.getOsallistujamaara())
                .build();
    }

    @Transactional
    // Haetaan yhden kauden tiedot
    public ResponseKausiDTO getKausiById(Long kausiId) {
        Kausi kausi = kausiRepository.findById(kausiId).orElse(null);
        if (kausi == null) {
            throw new ApiException(HttpStatus.NOT_FOUND, "Kautta ei löytynyt ID:llä " + kausiId);
        }

        return mapToDto(kausi);
    }

    @Transactional
    // Haetaan kaikki kaudet
    public List<ResponseKausiDTO> getAllKausi() {
        List<ResponseKausiDTO> kausilista = kausiRepository.findAll()
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());

        if (kausilista.isEmpty())
            return List.of();

        return kausilista;
    }

    @Transactional
    // Haetaan nykyinen kausi
    public ResponseKausiDTO getCurrentKausi() {
        Kausi kausi = kausiRepository.findTopByOrderByKausiIdDesc();

        if (kausi == null) {
            throw new ApiException(HttpStatus.NOT_FOUND, "Yhtään kautta ei ole vielä tallennettu");
        }

        return mapToDto(kausi);
    }

    @Transactional
    // Lisää uusi kausi
    public ResponseKausiDTO addNewKausi(UusiKausiDTO dto) {

        String nimi = Optional.ofNullable(dto.getNimi())
                .map(String::trim)
                .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "Kauden nimi puuttuu"));

        Kausi kausi = new Kausi();
        kausi.setNimi(nimi);
        kausi.setGpMaara(0);
        kausi.setSuunniteltuGpMaara(dto.getSuunniteltuGpMaara());
        kausi.setOsallistujamaara(dto.getOsallistujamaara());
        Kausi uusiKausi = kausiRepository.save(kausi);

        return mapToDto(uusiKausi);
    }

    @Transactional
    // Päivitä kausi
    public ResponseKausiDTO updateKausi(Long kausiId, UusiKausiDTO dto) {
        Kausi kausi = kausiRepository.findById(kausiId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Kautta ei löytynyt ID:llä " + kausiId));

        String uusiNimi = Optional.ofNullable(dto.getNimi())
                .map(String::trim)
                .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "Kauden nimi puuttuu"));

        kausi.setNimi(uusiNimi);
        kausi.setSuunniteltuGpMaara(dto.getSuunniteltuGpMaara());
        kausi.setOsallistujamaara(dto.getOsallistujamaara());

        Kausi paivitettyKausi = kausiRepository.save(kausi);

        return mapToDto(paivitettyKausi);
    }

    @Transactional
    // Poista kausi
    public void deleteKausi(Long kausiId) {
        Kausi kausi = kausiRepository.findById(kausiId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Kautta ei löytynyt ID:llä " + kausiId));

        kausiRepository.delete(kausi);
    }
}

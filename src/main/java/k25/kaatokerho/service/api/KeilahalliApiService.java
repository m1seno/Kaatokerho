package k25.kaatokerho.service.api;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import k25.kaatokerho.domain.Keilahalli;
import k25.kaatokerho.domain.KeilahalliRepository;
import k25.kaatokerho.domain.dto.ResponseKeilahalliDTO;
import k25.kaatokerho.domain.dto.UusiKeilahalliDTO;
import k25.kaatokerho.exception.ApiException;

@Service
public class KeilahalliApiService {

    private final KeilahalliRepository keilahalliRepo;

    public KeilahalliApiService(KeilahalliRepository keilahalliRepo) {
        this.keilahalliRepo = keilahalliRepo;
    }

    private ResponseKeilahalliDTO mapToDto(Keilahalli keilahalli) {
        return ResponseKeilahalliDTO.builder()
                .keilahalliId(keilahalli.getKeilahalliId())
                .nimi(keilahalli.getNimi())
                .kaupunki(keilahalli.getKaupunki())
                .valtio(keilahalli.getValtio())
                .build();
    }

    // Haetaan lista kaikista keilahalleista
    public List<ResponseKeilahalliDTO> getAllKeilahallit() {
        List<Keilahalli> keilahalliLista = new ArrayList<>();
        keilahalliRepo.findAll().forEach(keilahalliLista::add);

        return keilahalliLista.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    // Haetaan yhden keilahallin tiedot
    public ResponseKeilahalliDTO getKeilahalliById(Long keilahalliId) {
        Keilahalli keilahalli = keilahalliRepo.findById(keilahalliId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Keilahallia ei löydy id:llä " + keilahalliId));

        return mapToDto(keilahalli);
    }

    // Lisää uusi keilahalli
    public ResponseKeilahalliDTO addNewKeilahalli(UusiKeilahalliDTO dto) {

        Keilahalli keilahalli = new Keilahalli();
        keilahalli.setNimi(dto.getNimi());
        keilahalli.setKaupunki(dto.getKaupunki());
        keilahalli.setValtio(dto.getValtio());

        return mapToDto(keilahalliRepo.save(keilahalli));
    }

    //Päivitä keilahalli
    public ResponseKeilahalliDTO updateKeilahalli(Long keilahalliId, UusiKeilahalliDTO dto) {
        Keilahalli keilahalli = keilahalliRepo.findById(keilahalliId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Keilahallia ei löytynyt ID:llä " + keilahalliId));

        keilahalli.setNimi(dto.getNimi());
        keilahalli.setKaupunki(dto.getKaupunki());
        keilahalli.setValtio(dto.getValtio());

        Keilahalli paivitettyKeilahalli = keilahalliRepo.save(keilahalli);

        return mapToDto(paivitettyKeilahalli);
    }

    // Poista keilahalli
    public void deleteKeilahalli(Long keilahalliId) {
        Keilahalli keilahalli = keilahalliRepo.findById(keilahalliId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Keilahallia ei löytynyt ID:llä " + keilahalliId));

        keilahalliRepo.delete(keilahalli);
    }


}

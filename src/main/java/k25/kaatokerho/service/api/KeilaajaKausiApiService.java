package k25.kaatokerho.service.api;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import k25.kaatokerho.domain.Kausi;
import k25.kaatokerho.domain.KausiRepository;
import k25.kaatokerho.domain.Keilaaja;
import k25.kaatokerho.domain.KeilaajaKausi;
import k25.kaatokerho.domain.KeilaajaKausiRepository;
import k25.kaatokerho.domain.KeilaajaRepository;
import k25.kaatokerho.domain.dto.ResponseKeilaajaKausiDTO;
import k25.kaatokerho.exception.ApiException;

@Service
public class KeilaajaKausiApiService {

    private final KeilaajaKausiRepository kkRepo;
    private final KausiRepository kausiRepo;
    private final KeilaajaRepository keilaajaRepo;

    public KeilaajaKausiApiService(KeilaajaKausiRepository kkRepo, KausiRepository kausiRepo,
            KeilaajaRepository keilaajaRepo) {
        this.kkRepo = kkRepo;
        this.kausiRepo = kausiRepo;
        this.keilaajaRepo = keilaajaRepo;
    }

    private ResponseKeilaajaKausiDTO mapToDto(KeilaajaKausi keilaajaKausi) {
        return ResponseKeilaajaKausiDTO.builder()
                .keilaajaKausiId(keilaajaKausi.getKeilaajaKausiId())
                .keilaajaId(keilaajaKausi.getKeilaaja().getKeilaajaId())
                .keilaajaNimi(
                        keilaajaKausi.getKeilaaja().getEtunimi() + " " + keilaajaKausi.getKeilaaja().getSukunimi())
                .kausiId(keilaajaKausi.getKausi().getKausiId())
                .kausiNimi(keilaajaKausi.getKausi().getNimi())
                .parasSarja(keilaajaKausi.getParasSarja())
                .huonoinSarja(keilaajaKausi.getHuonoinSarja())
                .kaudenPisteet(keilaajaKausi.getKaudenPisteet())
                .voittoja(keilaajaKausi.getVoittoja())
                .osallistumisia(keilaajaKausi.getOsallistumisia())
                .build();
    }

    // Hakee kaikki keilaajakausitiedot (kaikista kausista ja keilaajista)
    public List<ResponseKeilaajaKausiDTO> getAllKeilaajaKaudet() {
        List<KeilaajaKausi> kkLista = new ArrayList<>();
        kkRepo.findAll().forEach(kkLista::add);

        return kkLista.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    // Hakee kauden kaikkien keilaajien tilastot (esim. sarjataulukkoa varten)
    public List<ResponseKeilaajaKausiDTO> getKaudenKeilaajat(Long kausiId) {
        Kausi kausi = kausiRepo.findById(kausiId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Kautta ei löytynyt ID:llä " + kausiId));

        List<KeilaajaKausi> kkLista = kkRepo.findByKausi_KausiId(kausiId);

        if (kkLista.isEmpty()) {
            throw new ApiException(HttpStatus.NOT_FOUND, "Tilastoja ei löydy kaudelta " + kausi.getNimi());
        }

        return kkLista.stream()
                .map(this::mapToDto)
                .toList();
    }

    // Hakee yksittäisen keilaajan tilastot kaikilta kausilta
    public List<ResponseKeilaajaKausiDTO> getKeilaajanKaudet(Long keilaajaId) {
        Keilaaja keilaaja = keilaajaRepo.findById(keilaajaId)
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Keilaajaa ei löytynyt ID:llä " + keilaajaId));

        List<KeilaajaKausi> kkLista = kkRepo.findByKeilaaja_KeilaajaId(keilaajaId);

        if (kkLista.isEmpty()) {
            throw new ApiException(HttpStatus.NOT_FOUND, "Keilaajalla " + keilaaja.getEtunimi() + " " + keilaaja.getSukunimi() + " ei ole kausitilastoja.");
        }

        return kkLista.stream()
                .map(this::mapToDto)
                .toList();
    }

    // Hakee tietyn keilaajan kausitilastot tietyltä kaudelta
    public ResponseKeilaajaKausiDTO getKeilaajanKausi(Long keilaajaId, Long kausiId) {
        Keilaaja keilaaja = keilaajaRepo.findById(keilaajaId)
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Keilaajaa ei löytynyt ID:llä " + keilaajaId));

        Kausi kausi = kausiRepo.findById(kausiId)
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Kautta ei löytynyt ID:llä " + kausiId));

        KeilaajaKausi keilaajaKausi = kkRepo.findByKeilaajaAndKausi(keilaaja, kausi)
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Keilaajan " + keilaaja.getEtunimi() + " " + keilaaja.getSukunimi() + " tilastoja ei löydy kaudelta " + kausi.getNimi()));

        return mapToDto(keilaajaKausi);
    }

    // Poista KeilaajaKausi
    public void deleteKeilaajaKausi(Long keilaajaKausiId)  {
        KeilaajaKausi keilaajaKausi = kkRepo.findById(keilaajaKausiId)
                    .orElseThrow(
                        () -> new ApiException(HttpStatus.NOT_FOUND, "KeilaajaKausi-instanssia ei löytynyt id:llä " + keilaajaKausiId));

        kkRepo.delete(keilaajaKausi);
    }

    

}

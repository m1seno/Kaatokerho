package k25.kaatokerho.web;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import k25.kaatokerho.domain.dto.KeilaajaKausiResponseDTO;
import k25.kaatokerho.service.api.KeilaajaKausiApiService;

@RestController
@RequestMapping("api/keilaajakausi")
public class KeilaajaKausiController {

    private final KeilaajaKausiApiService keilaajaKausiApiService;

    public KeilaajaKausiController(KeilaajaKausiApiService keilaajaKausiApiService) {
        this.keilaajaKausiApiService = keilaajaKausiApiService;
    }

    // Hakee kaikki keilaajakausitiedot (kaikista kausista ja keilaajista)
    @GetMapping
    public ResponseEntity<List<KeilaajaKausiResponseDTO>> getAllKeilaajaKaudet() {
        List<KeilaajaKausiResponseDTO> kkLista = keilaajaKausiApiService.getAllKeilaajaKaudet();
        return ResponseEntity.ok(kkLista);
    }


    // Hakee kauden kaikkien keilaajien tilastot (esim. sarjataulukkoa varten)
    @GetMapping("/kausi/{kausiId}")
    public ResponseEntity<List<KeilaajaKausiResponseDTO>> getKaudenKeilaajat(Long kausiId) {
        List<KeilaajaKausiResponseDTO> kausiKeilaajat = keilaajaKausiApiService.getKaudenKeilaajat(kausiId);
        return ResponseEntity.ok(kausiKeilaajat);
    }

    // Hakee yksittäisen keilaajan tilastot kaikilta kausilta
    @GetMapping("/keilaaja/{keilaajaId}")
    public ResponseEntity<List<KeilaajaKausiResponseDTO>> getKeilaajanKaudet(Long keilaajaId) {
        List<KeilaajaKausiResponseDTO> keilaajaKaudet = keilaajaKausiApiService.getKeilaajanKaudet(keilaajaId);
        return ResponseEntity.ok(keilaajaKaudet);
    }

    // Hakee tietyn keilaajan kausitilastot tietyltä kaudelta
    @GetMapping("/keilaaja/{keilaajaId}/kausi/{kausiId}")
    public ResponseEntity<KeilaajaKausiResponseDTO> getKeilaajanKausi(Long keilaajaId, Long kausiId) {
        KeilaajaKausiResponseDTO keilaajaKausi = keilaajaKausiApiService.getKeilaajanKausi(keilaajaId, kausiId);
        return ResponseEntity.ok(keilaajaKausi);
    }
}

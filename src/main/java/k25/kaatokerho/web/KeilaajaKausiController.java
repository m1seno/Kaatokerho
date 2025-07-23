package k25.kaatokerho.web;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import k25.kaatokerho.domain.KeilaajaKausiRepository;
import k25.kaatokerho.domain.dto.ResponseKeilaajaKausiDTO;
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
    public ResponseEntity<List<ResponseKeilaajaKausiDTO>> getAllKeilaajaKaudet() {
        List<ResponseKeilaajaKausiDTO> kkLista = keilaajaKausiApiService.getAllKeilaajaKaudet();
        return ResponseEntity.ok(kkLista);
    }


    // Hakee kauden kaikkien keilaajien tilastot (esim. sarjataulukkoa varten)
    @GetMapping("/kausi/{kausiId}")
    public ResponseEntity<List<ResponseKeilaajaKausiDTO>> getKaudenKeilaajat(@PathVariable Long kausiId) {
        List<ResponseKeilaajaKausiDTO> kausiKeilaajat = keilaajaKausiApiService.getKaudenKeilaajat(kausiId);
        return ResponseEntity.ok(kausiKeilaajat);
    }

    // Hakee yksittäisen keilaajan tilastot kaikilta kausilta
    @GetMapping("/keilaaja/{keilaajaId}")
    public ResponseEntity<List<ResponseKeilaajaKausiDTO>> getKeilaajanKaudet(@PathVariable Long keilaajaId) {
        List<ResponseKeilaajaKausiDTO> keilaajaKaudet = keilaajaKausiApiService.getKeilaajanKaudet(keilaajaId);
        return ResponseEntity.ok(keilaajaKaudet);
    }

    // Hakee tietyn keilaajan kausitilastot tietyltä kaudelta
    @GetMapping("/keilaaja/{keilaajaId}/kausi/{kausiId}")
    public ResponseEntity<ResponseKeilaajaKausiDTO> getKeilaajanKausi(@PathVariable Long keilaajaId, Long kausiId) {
        ResponseKeilaajaKausiDTO keilaajaKausi = keilaajaKausiApiService.getKeilaajanKausi(keilaajaId, kausiId);
        return ResponseEntity.ok(keilaajaKausi);
    }

    // Poistaa KeilaajaKausi
    @DeleteMapping("/{keilaajaKausiId}")
    public ResponseEntity<Void> poistaKeilaajaKausi(@PathVariable Long keilaajaKausiId) {
        keilaajaKausiApiService.deleteKeilaajaKausi(keilaajaKausiId);
        return ResponseEntity.noContent().build();
    }
}

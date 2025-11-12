package k25.kaatokerho.web;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import k25.kaatokerho.domain.dto.ResponseKultainenGpDTO;
import k25.kaatokerho.service.api.KultainenGpApiService;

@RestController
@RequestMapping("/api/kultainengp")
public class KultainenGpController {

    private final KultainenGpApiService kultainenService;

    public KultainenGpController(KultainenGpApiService kultainenService) {
        this.kultainenService = kultainenService;
    }

    // Lista kaikista kultainenGp -instansseista
    @GetMapping
    public ResponseEntity<List<ResponseKultainenGpDTO>> getAllKultainenGp() {
        List<ResponseKultainenGpDTO> kgbLista = kultainenService.getAllKGP();
        return ResponseEntity.ok(kgbLista);
    }

    // Lista kaikista GP-kohtaisista KGP-instansseista
    @GetMapping("/gp/{gpId}")
    public ResponseEntity<List<ResponseKultainenGpDTO>> getGpKohtaiset(@PathVariable Long gpId) {
        List<ResponseKultainenGpDTO> kgbLista = kultainenService.getGPkohtaiset(gpId);
        return ResponseEntity.ok(kgbLista);
    }

    // Lista kaikista kausikohtaisista KGP-instansseista
    @GetMapping("/kausi/{kausiId}")
    public ResponseEntity<List<ResponseKultainenGpDTO>> getKaudenKGP(@PathVariable Long kausiId) {
        List<ResponseKultainenGpDTO> kgbLista = kultainenService.getKaudenKGP(kausiId);
        return ResponseEntity.ok(kgbLista);
    }

    // Lista kaikista keilaajakohtaisista KGP-instansseista
    @GetMapping("/keilaaja/{keilaajaId}")
    public ResponseEntity<List<ResponseKultainenGpDTO>> getKeilaajanKGP(@PathVariable Long keilaajaId) {
        List<ResponseKultainenGpDTO> kgbLista = kultainenService.getKeilaajanKGP(keilaajaId);
        return ResponseEntity.ok(kgbLista);
    }

    // Hakee tietyn keilaajan kausitilastot tietyltä kaudelta
    @GetMapping("/keilaaja/{keilaajaId}/kausi/{kausiId}")
    public ResponseEntity<List<ResponseKultainenGpDTO>> getKeilaajanKausiKGP(@PathVariable Long keilaajaId, @PathVariable Long kausiId) {
        List<ResponseKultainenGpDTO> keilaajakausiKGP = kultainenService.getKeilaajanKausiKGP(keilaajaId, kausiId);
        return ResponseEntity.ok(keilaajakausiKGP);
    }

    // Poistaa KultaisenGp-instanssin
    @DeleteMapping("/{kultainenGpId}")
    public ResponseEntity<Void> poistaKultainenGp(@PathVariable Long kultainenGpId) {
        kultainenService.deleteKultainenGpIfExists(kultainenGpId);
        return ResponseEntity.noContent().build();
    }
}
package k25.kaatokerho.web;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import k25.kaatokerho.domain.dto.ResponseKausiDTO;
import k25.kaatokerho.domain.dto.UusiKausiDTO;
import k25.kaatokerho.service.api.KausiApiService;

@RestController
@RequestMapping("/api/kausi")
public class KausiController {

    private final KausiApiService kausiService;

    public KausiController(KausiApiService kausiService) {
        this.kausiService = kausiService;
    }

    // Lista kaikista kausista
    @GetMapping
    public ResponseEntity<List<ResponseKausiDTO>> haeKaikkiKaudet() {
        return ResponseEntity.ok(kausiService.getAllKausi());
    }

    // Nykyinen kausi
    @GetMapping("/current")
    public ResponseEntity<ResponseKausiDTO> haeNykyinenKausi() {
        return ResponseEntity.ok(kausiService.getCurrentKausi());
    }

    // Kausi Id:n perusteella
    @GetMapping("/{id}")
    public ResponseEntity<ResponseKausiDTO> haeKausi(@PathVariable Long id) {
        ResponseKausiDTO kausiDTO = kausiService.getKausiById(id);
        return ResponseEntity.ok(kausiDTO);
    }

    // Lisää uusi kausi
    @PostMapping
    public ResponseEntity<ResponseKausiDTO> lisaaUusiKausi(@Valid @RequestBody UusiKausiDTO kausi) {
        ResponseKausiDTO tallennettuKausi = kausiService.addNewKausi(kausi);
        return ResponseEntity.status(HttpStatus.CREATED).body(tallennettuKausi);
    }

    // Päivitä kausi
    @PutMapping("/{id}")
    public ResponseEntity<ResponseKausiDTO> paivitaKausi(@PathVariable Long id, @Valid @RequestBody UusiKausiDTO dto) {
        ResponseKausiDTO paivitettyKausi = kausiService.updateKausi(id, dto);
        return ResponseEntity.ok(paivitettyKausi);
    }

    // Poista kausi
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> poistaKausi(@PathVariable Long id) {
        kausiService.deleteKausi(id);
        return ResponseEntity.noContent().build();
    }
}

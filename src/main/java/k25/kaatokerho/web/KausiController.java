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
import k25.kaatokerho.domain.dto.KausiResponseDTO;
import k25.kaatokerho.domain.dto.UusiKausiDTO;
import k25.kaatokerho.service.KausiService;

@RestController
@RequestMapping("/api/kausi")
public class KausiController {

    private final KausiService kausiService;

    public KausiController(KausiService kausiService) {
        this.kausiService = kausiService;
    }

    // Lista kaikista kausista
    @GetMapping
    public ResponseEntity<List<KausiResponseDTO>> haeKaikkiKaudet() {
        return ResponseEntity.ok(kausiService.getAllKausi());
    }

    // Nykyinen kausi
    @GetMapping("/current")
    public ResponseEntity<KausiResponseDTO> haeNykyinenKausi() {
        return ResponseEntity.ok(kausiService.getCurrentKausi());
    }

    // Kusi Id:n perusteella
    @GetMapping("/{id}")
    public ResponseEntity<KausiResponseDTO> haeKausi(@PathVariable Long id) {
        KausiResponseDTO kausiDTO = kausiService.getKausiById(id);
        return ResponseEntity.ok(kausiDTO);
    }

    // Lisää uusi kausi
    @PostMapping
    public ResponseEntity<KausiResponseDTO> lisaaUusiKausi(@Valid @RequestBody UusiKausiDTO kausi) {
        KausiResponseDTO tallennettuKausi = kausiService.addNewKausi(kausi);
        return ResponseEntity.status(HttpStatus.CREATED).body(tallennettuKausi);
    }

    // Päivitä kausi
    @PutMapping("/{id}")
    public ResponseEntity<KausiResponseDTO> paivitaKausi(@PathVariable Long id, @Valid @RequestBody UusiKausiDTO dto) {
        KausiResponseDTO paivitettyKausi = kausiService.updateKausi(id, dto);
        return ResponseEntity.ok(paivitettyKausi);
    }

    // Poista kausi
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> poistaKausi(@PathVariable Long id) {
        kausiService.deleteKausi(id);
        return ResponseEntity.noContent().build();
    }
}

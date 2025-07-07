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
import k25.kaatokerho.domain.dto.ResponseKeilahalliDTO;
import k25.kaatokerho.domain.dto.UusiKeilahalliDTO;
import k25.kaatokerho.service.api.KeilahalliApiService;

@RestController
@RequestMapping("/api/keilahalli")
public class KeilahalliController {

    private final KeilahalliApiService keilahalliService;

    public KeilahalliController(KeilahalliApiService keilahalliService) {
        this.keilahalliService = keilahalliService;
    }

    // Lista kaikista keilahalleista
    @GetMapping
    public ResponseEntity<List<ResponseKeilahalliDTO>> haeKaikkiKaudet() {
        return ResponseEntity.ok(keilahalliService.getAllKeilahallit());
    }

    // Keilahalli Id:n perusteella
    @GetMapping("/{id}")
    public ResponseEntity<ResponseKeilahalliDTO> haeKeilahalli(@PathVariable Long id) {
        ResponseKeilahalliDTO keilahalliDTO = keilahalliService.getKeilahalliById(id);
        return ResponseEntity.ok(keilahalliDTO);
    }

    // Lisää uusi keilahalli
    @PostMapping
    public ResponseEntity<ResponseKeilahalliDTO> lisaaUusiKeilahalli(@Valid @RequestBody UusiKeilahalliDTO keilahalli) {
        ResponseKeilahalliDTO tallennettuKeilahalli = keilahalliService.addNewKeilahalli(keilahalli);
        return ResponseEntity.status(HttpStatus.CREATED).body(tallennettuKeilahalli);
    }

    // Päivitä keilahalli
    @PutMapping("/{id}")
    public ResponseEntity<ResponseKeilahalliDTO> paivitaKeilahalli(@PathVariable Long id, @Valid @RequestBody UusiKeilahalliDTO dto) {
        ResponseKeilahalliDTO paivitettyKeilahalli = keilahalliService.updateKeilahalli(id, dto);
        return ResponseEntity.ok(paivitettyKeilahalli);
    }

    // Poista keilahalli
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> poistaKeilahalli(@PathVariable Long id) {
        keilahalliService.deleteKeilahalli(id);
        return ResponseEntity.noContent().build();
    }
}

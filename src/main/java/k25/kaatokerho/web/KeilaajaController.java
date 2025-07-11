package k25.kaatokerho.web;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import k25.kaatokerho.domain.dto.ResponseKeilaajaDTO;
import k25.kaatokerho.domain.dto.PaivitaSalasanaDTO;
import k25.kaatokerho.domain.dto.UusiKeilaajaDTO;
import k25.kaatokerho.service.api.KeilaajaApiService;

@RestController
@RequestMapping("api/keilaaja")
public class KeilaajaController {

    private final KeilaajaApiService keilaajaService;

    public KeilaajaController(KeilaajaApiService keilaajaService) {
        this.keilaajaService = keilaajaService;
    }

    // Hae lista kaikista keilaajista
    @GetMapping
    public ResponseEntity<List<ResponseKeilaajaDTO>> getAllKeilaajat() {
        return ResponseEntity.ok(keilaajaService.getAllKeilaajat());
    }

    // Hae keilaaja Id:n perusteella
    @GetMapping("/{id}")
    public ResponseEntity<ResponseKeilaajaDTO> getKeilaaja(@PathVariable Long id) {
        return ResponseEntity.ok(keilaajaService.getKeilaajaById(id));
    }

    // Lisää uusi keilaaja
    @PostMapping
    public ResponseEntity<ResponseKeilaajaDTO> addNewKeilaaja(@Valid @RequestBody UusiKeilaajaDTO dto) {
        ResponseKeilaajaDTO tallennettuKeilaaja = keilaajaService.addNewKeilaaja(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(tallennettuKeilaaja);
    }

    // Muokkaa keilaajaa
    @PutMapping("/{id}")
    public ResponseEntity<ResponseKeilaajaDTO> editKeilaaja(@PathVariable Long id, @Valid @RequestBody UusiKeilaajaDTO dto) {
        ResponseKeilaajaDTO updatedKeilaaja = keilaajaService.updateKeilaaja(id, dto);
        return ResponseEntity.ok(updatedKeilaaja);
    }

    // Vaihda salasana
    @PutMapping("/{id}/salasana")
    public ResponseEntity<Map<String, String>> updateSalasana(@PathVariable Long id, @Valid @RequestBody PaivitaSalasanaDTO dto){
        keilaajaService.updateSalasana(id, dto);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Salasana päivitetty onnistuneesti!");

        return ResponseEntity.ok(response);
    }

    // Poista Keilaaja
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteKeilaaja(@PathVariable Long id){
        keilaajaService.deleteKeilaaja(id);
        return ResponseEntity.noContent().build();
    }

}

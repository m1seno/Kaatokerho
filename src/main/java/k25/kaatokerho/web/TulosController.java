package k25.kaatokerho.web;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import k25.kaatokerho.domain.dto.LisaaTuloksetDTO;
import k25.kaatokerho.domain.dto.TulosResponseDTO;
import k25.kaatokerho.service.api.TulosApiService;

@RestController
@RequestMapping("/api/tulokset")
@Validated
public class TulosController {

    private final TulosApiService tulosService;

    public TulosController(TulosApiService tulosService) {
        this.tulosService = tulosService;
    }

    /**
     * Lisää/korvaa yhden GP:n tulokset kerralla.
     * Idempotentti: poistaa ensin vanhat tulokset tältä GP:ltä ja tallentaa uudet.
     */
    @PostMapping
    public ResponseEntity<List<TulosResponseDTO>> lisaaTaiKorvaaGpTulokset(@Valid @RequestBody LisaaTuloksetDTO dto) {
        List<TulosResponseDTO> tallennetut = tulosService.LisaaTaiKorvaaGpTulokset(dto);
        return ResponseEntity.ok(tallennetut);
    }

    /** Hakee tietyn GP:n tulokset. */
    @GetMapping("/gp/{gpId}")
    public ResponseEntity<List<TulosResponseDTO>> haeGpTulokset(@PathVariable Long gpId) {
        return ResponseEntity.ok(tulosService.haeTuloksetGp(gpId));
    }

    /** Poistaa tietyn GP:n tulokset. */
    @DeleteMapping("/gp/{gpId}")
    public ResponseEntity<Void> poistaGpTulokset(@PathVariable Long gpId) {
        tulosService.poistaTuloksetGp(gpId);
        return ResponseEntity.noContent().build();
    }

    /** Hakee keilaajan kaikki tulokset. */
    @GetMapping("/keilaaja/{keilaajaId}")
    public ResponseEntity<List<TulosResponseDTO>> haeKeilaajanTulokset(@PathVariable Long keilaajaId) {
        return ResponseEntity.ok(tulosService.haeKeilaajanTulokset(keilaajaId));
    }

    /** Hakee keilaajan tulokset tietyllä kaudella. */
    @GetMapping("/keilaaja/{keilaajaId}/kausi/{kausiId}")
    public ResponseEntity<List<TulosResponseDTO>> haeKeilaajanTuloksetKaudella(
            @PathVariable Long keilaajaId,
            @PathVariable Long kausiId) {
        return ResponseEntity.ok(tulosService.haeKeilaajanTuloksetKaudella(keilaajaId, kausiId));
    }
}
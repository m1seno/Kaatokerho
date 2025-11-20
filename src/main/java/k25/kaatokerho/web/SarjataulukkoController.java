package k25.kaatokerho.web;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import k25.kaatokerho.domain.dto.SarjataulukkoDTO;
import k25.kaatokerho.service.SarjataulukkoService;

@RestController
@RequestMapping("/api/sarjataulukko")
public class SarjataulukkoController {

    private final SarjataulukkoService sarjataulukkoService;

    public SarjataulukkoController(SarjataulukkoService sarjataulukkoService) {
        this.sarjataulukkoService = sarjataulukkoService;
    }

    // Kuluvan kauden sarjataulukko
    @GetMapping("/current")
    public ResponseEntity<List<SarjataulukkoDTO>> getCurrentSeasonTable() {
        return ResponseEntity.ok(sarjataulukkoService.haeSarjataulukkoNykyinenKausi());
    }

    // Kuluvan kauden GP-numeroiden lista (pylväitä / kolumneja varten)
    @GetMapping("/current/gp-numerot")
    public ResponseEntity<List<Integer>> getCurrentSeasonGpNumbers() {
        return ResponseEntity.ok(sarjataulukkoService.haeJarjestysnumerotNykyinenKausi());
    }
}

package k25.kaatokerho.web;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import k25.kaatokerho.domain.dto.KalenteriDTO;
import k25.kaatokerho.service.KalenteriService;

@RestController
@RequestMapping("/api/kalenteri")
public class KalenteriController {

    private final KalenteriService kalenteriService;

    public KalenteriController(KalenteriService kalenteriService) {
        this.kalenteriService = kalenteriService;
    }

    // Kuluvan kauden kilpailukalenteri voittajineen
    @GetMapping("/current")
    public ResponseEntity<List<KalenteriDTO>> getCurrentSeasonCalendar() {
        List<KalenteriDTO> lista = kalenteriService.kuluvanKaudenKalenteri();
        return ResponseEntity.ok(lista);
    }

    // Tietyn kauden kilpailukalenteri voittajineen
    @GetMapping("/kausi/{kausiId}")
    public ResponseEntity<List<KalenteriDTO>> getSeasonCalendar(@PathVariable Long kausiId) {
        List<KalenteriDTO> lista = kalenteriService.tietynKaudenKalenteri(kausiId);
        return ResponseEntity.ok(lista);
    }

    // Kuluvan kauden voittotulosten keskiarvo
    @GetMapping("/current/average-win")
    public ResponseEntity<Double> getCurrentSeasonAverageWin() {
        List<KalenteriDTO> lista = kalenteriService.kuluvanKaudenKalenteri();
        double avg = kalenteriService.laskeKeskiarvo(lista);
        return ResponseEntity.ok(avg);
    }

    // Tietyn kauden voittotulosten keskiarvo
    @GetMapping("/kausi/{kausiId}/average-win")
    public ResponseEntity<Double> getSeasonAverageWin(@PathVariable Long kausiId) {
        List<KalenteriDTO> lista = kalenteriService.tietynKaudenKalenteri(kausiId);
        double avg = kalenteriService.laskeKeskiarvo(lista);
        return ResponseEntity.ok(avg);
    }
}
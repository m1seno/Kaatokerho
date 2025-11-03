package k25.kaatokerho.web;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import k25.kaatokerho.domain.dto.KuppiksenKunkkuDTO;
import k25.kaatokerho.domain.dto.KuppiksenKunkkuStatsDTO;
import k25.kaatokerho.service.api.KuppiksenKunkkuApiService;

@RestController
@RequestMapping("/api/kk")
public class KuppiksenKunkkuController {

    private final KuppiksenKunkkuApiService apiService;

    public KuppiksenKunkkuController(KuppiksenKunkkuApiService apiService) {
        this.apiService = apiService;
    }

    // 1) Kauden historia aikajärjestyksessä (yleisin käyttö)
    @GetMapping("/history")
    public ResponseEntity<List<KuppiksenKunkkuDTO>> getSeasonHistory(
            @RequestParam(name = "season", required = true) String seasonName) {
        return ResponseEntity.ok(apiService.getSeasonHistory(seasonName));
    }

    // 2) Viimeisin (”nykyinen kunkku”) kaudella
    @GetMapping("/current")
    public ResponseEntity<KuppiksenKunkkuDTO> getCurrentChampion(
            @RequestParam(name = "season", required = true) String seasonName) {
        return ResponseEntity.ok(apiService.getCurrentChampion(seasonName));
    }

    // 3) KK-merkintä tietylle GP:lle
    @GetMapping("/gp/{gpId}")
    public ResponseEntity<KuppiksenKunkkuDTO> getByGp(@PathVariable Long gpId) {
        return ResponseEntity.ok(apiService.getByGp(gpId));
    }

    // 4) Pelaajakohtainen historia (kuinka usein puolustajana/haastajana/voittajana)
    @GetMapping("/player/{keilaajaId}")
    public ResponseEntity<List<KuppiksenKunkkuDTO>> getByPlayer(@PathVariable Long keilaajaId,
                                                                @RequestParam(name = "season", required = false) String seasonName) {
        return ResponseEntity.ok(apiService.getByPlayer(keilaajaId, seasonName));
    }

    // 5) (valinnainen) kausikooste/statistiikka
    @GetMapping("/stats")
    public ResponseEntity<KuppiksenKunkkuStatsDTO> getSeasonStats(@RequestParam("season") String seasonName) {
        return ResponseEntity.ok(apiService.getSeasonStats(seasonName));
    }

}
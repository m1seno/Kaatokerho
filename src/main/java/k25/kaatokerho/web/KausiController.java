package k25.kaatokerho.web;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import k25.kaatokerho.domain.Kausi;
import k25.kaatokerho.domain.KausiRepository;
import k25.kaatokerho.domain.dto.KausiDTO;

@RestController
@RequestMapping("/api/kausi")
public class KausiController {

    private final KausiRepository kausiRepository;

    public KausiController(KausiRepository kausiRepository) {
        this.kausiRepository = kausiRepository;
    }

    // Get list of all seasons
    @GetMapping
    public ResponseEntity<List<KausiDTO>> getAllKausi() {
        List<KausiDTO> kausiList = kausiRepository.findAll();

        return ResponseEntity.ok(kausiList);
    }

    // Get current season
    @GetMapping("/current")
    public ResponseEntity<Kausi> getCurrentKausi() {
        Kausi currentKausi = kausiRepository.findTopByOrderByKausiIdDesc();
        if (currentKausi != null) {
            return ResponseEntity.ok(currentKausi);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // Get season by Id
    @GetMapping("/{id}")
    public ResponseEntity<Kausi> getKausiById(@PathVariable Long id) {
        return kausiRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Add new season
    @PostMapping
    public ResponseEntity<Kausi> addNewKausi(Kausi kausi) {
        Kausi savedKausi = kausiRepository.save(kausi);
        return ResponseEntity.status(201).body(savedKausi);
    }
}

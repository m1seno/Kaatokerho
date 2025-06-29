package k25.kaatokerho.web;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import k25.kaatokerho.domain.Kausi;
import k25.kaatokerho.domain.KausiRepository;
import k25.kaatokerho.domain.dto.KausiDTO;
import k25.kaatokerho.domain.dto.UusiKausiDTO;
import k25.kaatokerho.service.KausiService;

@RestController
@RequestMapping("/api/kausi")
public class KausiController {

    private final KausiRepository kausiRepository;
    private final KausiService kausiService;

    public KausiController(KausiRepository kausiRepository, KausiService kausiService) {
        this.kausiRepository = kausiRepository;
        this.kausiService = kausiService;
    }

    // Lista kaikista kausista
    @GetMapping("/all")
    public ResponseEntity<List<KausiDTO>> haeKaikkiKaudet() {
        return ResponseEntity.ok(kausiService.getAllKausi());
    }

    // Nykyinen kausi
    @GetMapping("/current")
    public ResponseEntity<KausiDTO> haeNykyinenKausi() {
        return ResponseEntity.ok(kausiService.getCurrentKausi());
    }

    // Kusi Id:n perusteella
    @GetMapping("/{id}")
    public ResponseEntity<KausiDTO> haeKausi(@PathVariable Long id) {
        return ResponseEntity.ok(kausiService.getKausiById(id));
    }

    // Lisää uusi kausi
    @PostMapping
    public ResponseEntity<Kausi> lisaaUusiKausi(@Valid @RequestBody UusiKausiDTO kausi) {
        Kausi tallennettuKausi = kausiService.addNewKausi(kausi);
        return ResponseEntity.status(HttpStatus.CREATED).body(tallennettuKausi);
    }
}

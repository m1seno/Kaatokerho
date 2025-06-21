package k25.kaatokerho.web;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import k25.kaatokerho.domain.Kausi;
import k25.kaatokerho.domain.KausiRepository;

@RestController
@RequestMapping("/api/kausi")
public class KausiController {

    private final KausiRepository kausiRepository;

    public KausiController(KausiRepository kausiRepository) {
        this.kausiRepository = kausiRepository;
    }

    @GetMapping
    public ResponseEntity<List<Kausi>> haeKaikkiKausi() {
        List<Kausi> kausiLista = kausiRepository.findAll();

        return ResponseEntity.ok(kausiLista);
    }
}

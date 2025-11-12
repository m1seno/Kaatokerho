package k25.kaatokerho.web;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import jakarta.validation.Valid;
import k25.kaatokerho.domain.GP;
import k25.kaatokerho.domain.GpRepository;
import k25.kaatokerho.domain.KeilahalliRepository;
import k25.kaatokerho.domain.dto.PaivitaGpDTO;
import k25.kaatokerho.domain.dto.UusiGpDTO;
import k25.kaatokerho.service.api.GpApiService;
import k25.kaatokerho.service.api.KultainenGpApiService;
import k25.kaatokerho.service.KuppiksenKunkkuService;
import k25.kaatokerho.service.GpDeleteService;

@RestController
@RequestMapping("/api/gp")
public class GpController {

    private final GpRepository gpRepository;
    private final GpApiService gpApiService;
    private final KeilahalliRepository keilahalliRepository;
    private final GpDeleteService gpDeletionService;

    public GpController(GpRepository gpRepository, GpApiService gpApiService,
            KeilahalliRepository keilahalliRepository,
            GpDeleteService gpDeletionService) {
        this.gpRepository = gpRepository;
        this.gpApiService = gpApiService;
        this.keilahalliRepository = keilahalliRepository;
        this.gpDeletionService = gpDeletionService;
    }

    @GetMapping
    public ResponseEntity<List<GP>> haeKaikkiGp() {
        List<GP> gpLista = gpRepository.findAll();

        return ResponseEntity.ok(gpLista);
    }

    /** Hae tietyn kauden kaikki GP:t */
    @GetMapping("/kausi/{kausiId}")
    public ResponseEntity<List<GP>> haeGpKausella(@PathVariable Long kausiId) {
        List<GP> lista = gpApiService.haeGpKausella(kausiId);
        if (lista.isEmpty()) {
            return ResponseEntity.ok(List.of());
        }
        return ResponseEntity.ok(lista);
    }

    /** Hae nykyisen (viimeisimmän) kauden kaikki GP:t */
    @GetMapping("/kausi/current")
    public ResponseEntity<List<GP>> haeNykyisenKaudenGp() {
        List<GP> lista = gpApiService.haeNykyisenKaudenGp();
        return ResponseEntity.ok(lista);
    }

    @GetMapping("/{id}")
    public ResponseEntity<GP> haeGp(@PathVariable Long id) {
        return gpRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<GP> luoUusiGp(@Valid @RequestBody UusiGpDTO gpDTO) {
        GP tallennettuGp = gpApiService.tallennaGpJaPalauta(gpDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(tallennettuGp);
    }

    @PatchMapping("/{id}")
public ResponseEntity<GP> paivitaGp(@PathVariable Long id, @Valid @RequestBody PaivitaGpDTO dto) {
    GP gp = gpRepository.findById(id)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "GP:tä id:llä " + id + " ei löytynyt"));

    if (dto.getPvm() != null) {
        gp.setPvm(dto.getPvm());
    }
    if (dto.getKeilahalliId() != null) {
        var keilahalli = keilahalliRepository.findById(dto.getKeilahalliId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Keilahallia ei löytynyt id:llä " + dto.getKeilahalliId()));
        gp.setKeilahalli(keilahalli);
    }
    if (dto.getOnKultainenGp() != null) {
        gp = gpApiService.paivitaKultaisuus(gp, dto.getOnKultainenGp());
    }

    gpRepository.save(gp);
    return ResponseEntity.ok(gp);
}

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> poistaGp(@PathVariable Long id) {
        gpDeletionService.deleteGpCompletely(id);

        return ResponseEntity.noContent().build();
    }
}

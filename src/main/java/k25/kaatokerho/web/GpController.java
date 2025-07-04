package k25.kaatokerho.web;

import java.util.List;
import java.util.Optional;
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

@RestController
@RequestMapping("/api/gp")
public class GpController {

    private final GpRepository gpRepository;
    private final GpApiService lisaaGpService;
    private final KeilahalliRepository keilahalliRepository;

    public GpController(GpRepository gpRepository, GpApiService lisaaGpService,
                            KeilahalliRepository keilahalliRepository) {
        this.gpRepository = gpRepository;
        this.lisaaGpService = lisaaGpService;
        this.keilahalliRepository = keilahalliRepository;
    }

    @GetMapping
    public ResponseEntity<List<GP>> haeKaikkiGp(){
    List<GP> gpLista = gpRepository.findAll();

                return ResponseEntity.ok(gpLista);
    }

    @GetMapping("/{id}")
    public ResponseEntity<GP> haeGp(@PathVariable Long id) {
        return gpRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<GP> luoUusiGp(@Valid @RequestBody UusiGpDTO gpDTO) {
        GP tallennettuGp = lisaaGpService.tallennaGpJaPalauta(gpDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(tallennettuGp);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<GP> paivitaGp(@PathVariable Long id, @Valid @RequestBody PaivitaGpDTO gpDTO) {
        Optional<GP> olemassaoleva = gpRepository.findById(id);
        if (olemassaoleva.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "GP:tä id:llä " + id + " ei löytynyt");
        }

        GP gp = olemassaoleva.get();

        gp.setPvm(gpDTO.getPvm());
        gp.setOnKultainenGp(gpDTO.getOnKultainenGp());

        keilahalliRepository.findById(gpDTO.getKeilahalliId()).ifPresent(gp::setKeilahalli);
        gpRepository.save(gp);

        return ResponseEntity.ok(gp);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> poistaGp(@PathVariable Long id) {
        Optional<GP> gpOpt = gpRepository.findById(id);
        if (gpOpt.isPresent()) {
            gpRepository.delete(gpOpt.get());
            return ResponseEntity.noContent().build();
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "GP:tä id:llä " + id + " ei löytynyt");
        }
    }
}

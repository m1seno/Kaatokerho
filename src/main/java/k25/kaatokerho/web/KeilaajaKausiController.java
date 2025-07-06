package k25.kaatokerho.web;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import k25.kaatokerho.service.api.KeilaajaKausiApiService;

@RestController
@RequestMapping("api/keilaajakausi")
public class KeilaajaKausiController {

    private final KeilaajaKausiApiService keilaajaKausiApiService;

    public KeilaajaKausiController(KeilaajaKausiApiService keilaajaKausiApiService) {
        this.keilaajaKausiApiService = keilaajaKausiApiService;
    }

    // Hakee kaikki keilaajakausitiedot (kaikista kausista ja keilaajista)
    @GetMapping


    // Hakee kauden kaikkien keilaajien tilastot (esim. sarjataulukkoa varten)
    @GetMapping("/kausi/{kausiId}")


    // Hakee yksittäisen keilaajan tilastot kaikilta kausilta
    @GetMapping("/keilaaja/{keilaajaId}")

    // Hakee tietyn keilaajan kausitilastot tietyltä kaudelta
    @GetMapping("/keilaaja/{keilaajaId}/kausi/{kausiId}")
}

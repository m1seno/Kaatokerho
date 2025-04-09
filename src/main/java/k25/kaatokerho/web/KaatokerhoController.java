package k25.kaatokerho.web;

import java.security.Principal;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import k25.kaatokerho.domain.Keilaaja;
import k25.kaatokerho.domain.KeilaajaRepository;
import k25.kaatokerho.domain.dto.KalenteriDTO;
import k25.kaatokerho.service.KalenteriService;

@Controller
public class KaatokerhoController {

    private final KeilaajaRepository keilaajaRepository;
    private final KalenteriService kalenteriService;

    public KaatokerhoController(KeilaajaRepository keilaajaRepository, KalenteriService kalenteriService) {
        this.keilaajaRepository = keilaajaRepository;
        this.kalenteriService = kalenteriService;
    }

    @GetMapping({ "/", "/home" })
    public String homePage(@RequestParam(required = false) String logout, Model model, Principal principal) {
        List<KalenteriDTO> gpLista = kalenteriService.GpTiedot();
        model.addAttribute("gpLista", gpLista);

        //Lasketaan voittotulosten keskiarvo
        double keskiarvo = kalenteriService.laskeKeskiarvo(gpLista);
        model.addAttribute("keskiarvo", keskiarvo);

        // Tarkistetaan onko käyttäjä kirjautunut
        if (principal != null) {
            String kayttajanimi = principal.getName();
            model.addAttribute("loggedIn", true);

            // Haetaan kirjautunut keilaaja ja tarkistetaan admin-oikeudet
            Keilaaja kayttaja = keilaajaRepository.findByKayttajanimi(kayttajanimi);
            model.addAttribute("admin", kayttaja.getAdmin());
        } else {
            model.addAttribute("loggedIn", false);
        }

        if (logout != null) {
            model.addAttribute("logoutMessage", "Olet kirjautunut ulos onnistuneesti.");
        }

        return "home";
    }
}

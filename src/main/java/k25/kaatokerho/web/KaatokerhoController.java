package k25.kaatokerho.web;

import java.security.Principal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import k25.kaatokerho.domain.GP;
import k25.kaatokerho.domain.GpRepository;
import k25.kaatokerho.domain.Keilaaja;
import k25.kaatokerho.domain.KeilaajaRepository;

@Controller
public class KaatokerhoController {

    @Autowired
    private GpRepository gpRepository;

    @Autowired
    private KeilaajaRepository keilaajaRepository;

    @GetMapping({"/", "/home"})
    public String homePage(Model model, Principal principal) {
        List<GP> gpLista = (List<GP>) gpRepository.findAll();
        model.addAttribute("gpLista", gpLista);

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

        return "home";
    }
}

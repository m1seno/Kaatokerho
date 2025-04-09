package k25.kaatokerho.web;

import java.security.Principal;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import k25.kaatokerho.domain.GpRepository;
import k25.kaatokerho.domain.Keilaaja;
import k25.kaatokerho.domain.KeilaajaRepository;
import k25.kaatokerho.domain.dto.KalenteriDTO;
import k25.kaatokerho.domain.dto.SarjataulukkoDTO;
import k25.kaatokerho.service.KalenteriService;
import k25.kaatokerho.service.SarjataulukkoService;

@Controller
public class KaatokerhoController {

    private final KeilaajaRepository keilaajaRepository;
    private final KalenteriService kalenteriService;
    private final SarjataulukkoService sarjataulukkoService;
    private final GpRepository gpRepository;

    public KaatokerhoController(KeilaajaRepository keilaajaRepository, KalenteriService kalenteriService, SarjataulukkoService sarjataulukkoService, GpRepository gpRepository) {
        this.keilaajaRepository = keilaajaRepository;
        this.kalenteriService = kalenteriService;
        this.sarjataulukkoService = sarjataulukkoService;
        this.gpRepository = gpRepository;
    }

    //Etusivu
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

    //Sarjataulukko
    @GetMapping("/sarjataulukko")
    public String sarjataulukko (Model model) {
        List<SarjataulukkoDTO> sarjataulukko = sarjataulukkoService.haeSarjataulukko();
        model.addAttribute("sarjataulukko", sarjataulukko);

        //Tehdään lista GP:n järjestysnumeroista
        List <Integer> gpNumerot = sarjataulukkoService.haeJarjestysnumerot();
        model.addAttribute("gpNumerot", gpNumerot);

        return "sarjataulukko";
    }

}

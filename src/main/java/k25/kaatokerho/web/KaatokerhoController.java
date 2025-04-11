package k25.kaatokerho.web;

import java.security.Principal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.validation.Valid;
import k25.kaatokerho.domain.GP;
import k25.kaatokerho.domain.GpRepository;
import k25.kaatokerho.domain.Keilaaja;
import k25.kaatokerho.domain.KeilaajaRepository;
import k25.kaatokerho.domain.Keilahalli;
import k25.kaatokerho.domain.KeilahalliRepository;
import k25.kaatokerho.domain.dto.KalenteriDTO;
import k25.kaatokerho.domain.dto.LisaaTuloksetDTO;
import k25.kaatokerho.domain.dto.SarjataulukkoDTO;
import k25.kaatokerho.domain.dto.UusiGpDTO;
import k25.kaatokerho.service.KalenteriService;
import k25.kaatokerho.service.LisaaGpService;
import k25.kaatokerho.service.SarjataulukkoService;
import k25.kaatokerho.service.TulosService;

@Controller
public class KaatokerhoController {

    private final KeilahalliRepository keilahalliRepository;
    private final KeilaajaRepository keilaajaRepository;
    private final KalenteriService kalenteriService;
    private final SarjataulukkoService sarjataulukkoService;
    private final GpRepository gpRepository;
    private final LisaaGpService lisaaGpService;
    private final TulosService tulosService;

    public KaatokerhoController(KeilaajaRepository keilaajaRepository, KalenteriService kalenteriService,
            SarjataulukkoService sarjataulukkoService, GpRepository gpRepository, LisaaGpService lisaaGpService,
            KeilahalliRepository keilahalliRepository, TulosService tulosService) {
        this.keilaajaRepository = keilaajaRepository;
        this.kalenteriService = kalenteriService;
        this.sarjataulukkoService = sarjataulukkoService;
        this.gpRepository = gpRepository;
        this.lisaaGpService = lisaaGpService;
        this.keilahalliRepository = keilahalliRepository;
        this.tulosService = tulosService;
    }

    // Etusivu
    @GetMapping({ "/", "/home" })
    public String etusivu(@RequestParam(required = false) String logout, Model model, Principal principal) {
        List<KalenteriDTO> gpLista = kalenteriService.GpTiedot();
        model.addAttribute("gpLista", gpLista);

        // Lasketaan voittotulosten keskiarvo
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

    // Sarjataulukko
    @GetMapping("/sarjataulukko")
    public String sarjataulukko(Model model) {
        List<SarjataulukkoDTO> sarjataulukko = sarjataulukkoService.haeSarjataulukko();
        model.addAttribute("sarjataulukko", sarjataulukko);

        // Tehdään lista GP:n järjestysnumeroista
        List<Integer> gpNumerot = sarjataulukkoService.haeJarjestysnumerot();
        model.addAttribute("gpNumerot", gpNumerot);

        return "sarjataulukko";
    }

    // Crud-toiminnot

    // Näytetään lista GP:stä
    @GetMapping("/admin/gpLista")
    public String gpLista(Model model) {
        Iterable<GP> gpListaIterable = gpRepository.findAll();

        // Muutetaan Iterable listaksi
        List<GP> gpLista = StreamSupport
                .stream(gpListaIterable.spliterator(), false)
                .collect(Collectors.toList());

        model.addAttribute("gpLista", gpLista);
        return "gpLista";
    }

    // Näytetään lomake uuden GP:n luomista varten
    @GetMapping("/admin/gp/new")
    public String uusiGp(Model model) {
        UusiGpDTO gpDTO = lisaaGpService.luoUusiGp();
        model.addAttribute("gpDTO", gpDTO);
        model.addAttribute("keilahallit", keilahalliRepository.findAll());

        return "uusiGp";
    }

    // Tallennetaan uusi GP
    @PostMapping("/admin/gp/save")
    public String tallennaGp(@Valid @ModelAttribute("gpDTO") UusiGpDTO gpDTO, BindingResult bindingResult,
            Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("keilahallit", keilahalliRepository.findAll());
            return "uusiGp";
        }

        try {
            lisaaGpService.tallennaGp(gpDTO);
        } catch (IllegalStateException e) {
            bindingResult.rejectValue("jarjestysnumero", "error.gpDTO", e.getMessage());
            model.addAttribute("keilahallit", keilahalliRepository.findAll());
            return "uusiGp";
        }

        return "redirect:/admin/gpLista";
    }

    // Haetaan yksittäinen GP
    @GetMapping("/admin/gp/{id}/tulokset")
    public String syotaTulokset(@PathVariable Long id, Model model) {
        List<Keilaaja> keilaajat = (List<Keilaaja>) keilaajaRepository.findAll();

        LisaaTuloksetDTO dto = new LisaaTuloksetDTO();
        dto.setGpId(id);
        List<LisaaTuloksetDTO.TulosForm> tulosFormit = keilaajat.stream().map(k -> {
            LisaaTuloksetDTO.TulosForm tf = new LisaaTuloksetDTO.TulosForm();
            tf.setKeilaajaId(k.getKeilaajaId());
            return tf;
        }).toList();
        dto.setTulokset(tulosFormit);

        model.addAttribute("tuloksetForm", dto);
        model.addAttribute("keilaajat", keilaajat);
        return "syotaTulokset";
    }

    //Tallennetaan GP:n tulokset
    @PostMapping("/admin/gp/tulokset/save")
    public String tallennaTulokset(@Valid @ModelAttribute("dto") LisaaTuloksetDTO dto, BindingResult bindingResult,
            Model model) {
        if (bindingResult.hasErrors()) {
            List<Keilaaja> keilaajat = (List<Keilaaja>) keilaajaRepository.findAll();
            model.addAttribute("keilaajat", keilaajat);
            return "syotaTulokset";
        }
        tulosService.tallennaTulokset(dto);
        return "redirect:/admin/gpLista";
    }

    @GetMapping("/admin/gp/edit/{id}")
public String muokkaaGp(@PathVariable Long id, Model model) {
    GP gp = gpRepository.findById(id).orElseThrow();
    List<Keilahalli> keilahallit = (List<Keilahalli>) keilahalliRepository.findAll();
    model.addAttribute("gp", gp);
    model.addAttribute("keilahallit", keilahallit);
    return "muokkaaGp";
}

@PatchMapping("/admin/gp/update")
public String paivitaGp(@Valid @RequestParam Long gpId,
                        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate pvm,
                        @RequestParam Long keilahalliId,
                        @RequestParam(required = false) boolean onKultainenGp) {
    GP gp = gpRepository.findById(gpId).orElseThrow();
    Keilahalli halli = keilahalliRepository.findById(keilahalliId).orElseThrow();

    gp.setPvm(pvm);
    gp.setKeilahalli(halli);
    gp.setOnKultainenGp(onKultainenGp);

    gpRepository.save(gp);

    return "redirect:/admin/gpLista";
}

@DeleteMapping("/admin/gp/delete/{id}")
public String poistaGp(@PathVariable Long id) {
    gpRepository.deleteById(id);
    return "redirect:/admin/gpLista";
}


}
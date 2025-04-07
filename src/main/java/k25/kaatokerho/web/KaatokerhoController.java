package k25.kaatokerho.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class KaatokerhoController {

    @RequestMapping(value="/home")
    public String home() {
        return "home";
    }
}

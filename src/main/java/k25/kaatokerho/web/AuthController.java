package k25.kaatokerho.web;

import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import jakarta.validation.Valid;
import k25.kaatokerho.domain.Keilaaja;
import k25.kaatokerho.domain.dto.LoginDTO;
import k25.kaatokerho.domain.dto.ResponseKeilaajaDTO;
import k25.kaatokerho.domain.dto.ResponseLoginDTO;
import k25.kaatokerho.service.JwtService;
import k25.kaatokerho.domain.KeilaajaRepository;

@RestController
@RequestMapping("/api/login")
@Validated
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    private final KeilaajaRepository keilaajaRepo;

    public AuthController(AuthenticationManager authenticationManager, JwtService jwtService,
            UserDetailsService userDetailsService,
            KeilaajaRepository keilaajaRepo) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
        this.keilaajaRepo = keilaajaRepo;
    }

    @PostMapping
    public ResponseEntity<?> login(@Valid @RequestBody LoginDTO loginDTO) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginDTO.getKayttajanimi(), loginDTO.getSalasana()));
        } catch (BadCredentialsException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Bad credentials");
        }

        Optional<Keilaaja> keilaaja = keilaajaRepo.findByKayttajanimi(loginDTO.getKayttajanimi());

        ResponseKeilaajaDTO dto = new ResponseKeilaajaDTO(
            keilaaja.get().getKeilaajaId(),
            keilaaja.get().getEtunimi(),
            keilaaja.get().getSukunimi(),
            keilaaja.get().getSyntymapaiva(),
            keilaaja.get().getAktiivijasen(),
            keilaaja.get().getAdmin(),
            keilaaja.get().getKayttajanimi()
        );

        UserDetails userDetails = userDetailsService.loadUserByUsername(loginDTO.getKayttajanimi());
        String jwt = jwtService.generateToken(userDetails);
        return ResponseEntity.ok(new ResponseLoginDTO(jwt, dto));
    }
}

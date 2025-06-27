package k25.kaatokerho.web;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import k25.kaatokerho.domain.dto.LoginDTO;
import k25.kaatokerho.domain.dto.LoginResponseDTO;
import k25.kaatokerho.service.JwtService;

@RestController
@RequestMapping("/api/login")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    public AuthController(AuthenticationManager authenticationManager, JwtService jwtService, UserDetailsService userDetailsService) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    @PostMapping
    public ResponseEntity<?> login(@RequestBody LoginDTO loginDTO) {
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(loginDTO.getKayttajanimi(), loginDTO.getSalasana())
        );

        UserDetails userDetails = userDetailsService.loadUserByUsername(loginDTO.getKayttajanimi());
        String jwt = jwtService.generateToken(userDetails);
        
        return ResponseEntity.ok(new LoginResponseDTO(jwt));
    }
}

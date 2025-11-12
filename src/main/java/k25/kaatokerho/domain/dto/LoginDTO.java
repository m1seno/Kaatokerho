package k25.kaatokerho.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginDTO {

    @NotBlank(message = "Käyttäjänimi ei saa olla tyhjä")
    private String kayttajanimi;
    
    @NotBlank(message = "Salasana ei saa olla tyhjä")
    @Size(min = 8, max = 60, message = "Salasanan tulee olla vähintään 8 merkkiä pitkä")
    private String salasana;
}

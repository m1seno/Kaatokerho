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
public class PaivitaSalasanaDTO {

    @NotBlank(message = "Vanha salasana ei voi olla tyhjä")
    @Size(min = 8, max = 60, message = "Vanhan salasanan on oltava 8-60 merkkiä pitkä")
    private String vanhaSalasana;

    @NotBlank(message = "Uusi salasana ei voi olla tyhjä")
    @Size(min = 8, max = 60, message = "Uuden salasanan on oltava 8-60 merkkiä pitkä")
    private String uusiSalasana;
}

package k25.kaatokerho.domain.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KausiDTO {

    @NotEmpty(message = "Kauden nimi ei saa olla tyhjä")
    private String nimi;

    @NotNull(message = "GP määrä on pakollinen")
    private Integer gpMaara;

    @NotNull(message = "Suunniteltu GP määrä on pakollinen")
    private Integer suunniteltuGpMaara;

    @NotNull(message = "Osallistujamäärä on pakollinen")
    private Integer osallistujamaara;

}

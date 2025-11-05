package k25.kaatokerho.domain.dto;

import java.util.List;

import jakarta.validation.Valid;
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
public class LisaaTuloksetDTO {

    @NotNull(message = "GP id on pakollinen")
    private Long gpId;

    @NotEmpty(message = "Tulokset-lista ei saa olla tyhjä")
    @Valid
    private List<TulosForm> tulokset;

    @Data
    public static class TulosForm {
        @NotNull(message = "Keilaaja id on pakollinen")
        private Long keilaajaId;

        // Sarjat voivat olla null -> jos toinenkin on null, 'osallistui' = false
        private Integer sarja1;
        private Integer sarja2;
    }
}
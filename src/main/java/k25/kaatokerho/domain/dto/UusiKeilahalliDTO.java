package k25.kaatokerho.domain.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UusiKeilahalliDTO {

    @NotEmpty(message = "Keilahallille on annettava nimi")
    private String nimi;

    @NotEmpty(message = "Keilahallille on annettava kaupunki")
    private String kaupunki;

    @NotEmpty(message = "Keilahallille on annettava valtio")
    private String valtio;
}

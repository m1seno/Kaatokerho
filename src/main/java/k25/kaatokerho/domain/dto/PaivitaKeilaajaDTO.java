package k25.kaatokerho.domain.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaivitaKeilaajaDTO {

    @NotEmpty(message = "Keilaajalla on oltava etunimi")
    private String etunimi;

    @NotEmpty(message = "Keilaajalla on oltava sukunimi")
    private String sukunimi;

    @NotNull(message = "Syötä keilaajan syntymäpäivä")
    private LocalDate syntymapaiva;

    @NotNull(message = "Määritä, onko keilaaja aktiivijäsen")
    private Boolean aktiivijasen;

    @NotNull(message = "Määritä, onko keilaajalla admin-oikeudet")
    private Boolean admin;

    @NotEmpty(message = "Käyttäjänimi on pakollinen")
    @Size(max = 50)
    private String kayttajanimi;
}
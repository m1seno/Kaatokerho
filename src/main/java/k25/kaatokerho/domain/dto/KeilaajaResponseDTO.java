package k25.kaatokerho.domain.dto;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KeilaajaResponseDTO {

    private Long keilaajaId;

    private String etunimi;

    private String sukunimi;

    private LocalDate syntymapaiva;
    
    private Boolean aktiivijasen;

    private Boolean admin;

    private String kayttajanimi;
}

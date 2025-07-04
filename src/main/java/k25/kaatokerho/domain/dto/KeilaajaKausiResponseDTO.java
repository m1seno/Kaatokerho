package k25.kaatokerho.domain.dto;

import k25.kaatokerho.domain.Kausi;
import k25.kaatokerho.domain.Keilaaja;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KeilaajaKausiResponseDTO {

    private Keilaaja keilaaja;

    private Kausi kausi;
    
    private Integer parasSarja;

    private Integer huonoinSarja;

    private Double kaudenPisteet;

    private Integer voittoja;

    private Integer osallistumisia;

}

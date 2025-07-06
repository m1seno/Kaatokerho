package k25.kaatokerho.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KeilaajaKausiResponseDTO {

    private Long keilaajaKausiId;

    private Long keilaajaId;
    private String keilaajaNimi;

    private Long kausiId;
    private String kausiNimi;
    
    private Integer parasSarja;
    private Integer huonoinSarja;
    private Double kaudenPisteet;
    private Integer voittoja;
    private Integer osallistumisia;

}

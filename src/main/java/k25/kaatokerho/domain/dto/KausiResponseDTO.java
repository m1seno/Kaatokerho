package k25.kaatokerho.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KausiResponseDTO {

    private Long kausiId;

    private String nimi;

    private Integer gpMaara;

    private Integer suunniteltuGpMaara;

    private Integer osallistujamaara;

}

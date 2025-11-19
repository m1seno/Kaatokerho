package k25.kaatokerho.domain.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ResponseKultainenGpDTO {

    private Long kultainenGpId;
    
    private Long keilaajaId;
    private String keilaajaNimi;
    
    private Long gpId;
    private Integer jarjestysnumero;

    private Long kausiId;
    private String kausiNimi;

    private Double lisapisteet;
    
}

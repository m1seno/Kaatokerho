package k25.kaatokerho.domain.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ResponseKeilahalliDTO {

    private Long keilahalliId;
    private String nimi;
    private String kaupunki;
    private String valtio;
}

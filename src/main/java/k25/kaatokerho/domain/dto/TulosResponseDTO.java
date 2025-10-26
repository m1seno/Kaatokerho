package k25.kaatokerho.domain.dto;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class TulosResponseDTO {
    Long tulosId;
    Long gpId;

    Long keilaajaId;
    String keilaajaEtunimi;
    String keilaajaSukunimi;

    Integer sarja1;
    Integer sarja2;
    Boolean osallistui;
}
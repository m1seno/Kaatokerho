package k25.kaatokerho.domain.dto;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class NextGpDTO {
    private Long gpId;
    private Integer jarjestysnumero;
    private LocalDate pvm;

    private Long kausiId;
    private String kausiNimi;

    private Long keilahalliId;
    private String keilahalliNimi;

    private Boolean onKultainenGp;
}
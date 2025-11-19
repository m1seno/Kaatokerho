package k25.kaatokerho.domain.dto;

import java.time.LocalDate;
import java.util.List;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class KkHaastajalistaResponseDTO {
    Long gpId;
    Integer gpNo;
    LocalDate pvm;
    List<KkHaastajaDTO> haastajat;
}
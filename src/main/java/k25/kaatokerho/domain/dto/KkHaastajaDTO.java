package k25.kaatokerho.domain.dto;

import lombok.Builder;
import lombok.Value;
@Value
@Builder
public class KkHaastajaDTO {

    Long keilaajaId;
    String nimi;
    Integer sarja1; // parempi sarja edellisestä GP:stä
    Integer sarja2; // huonompi sarja
}

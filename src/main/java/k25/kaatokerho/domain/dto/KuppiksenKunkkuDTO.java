package k25.kaatokerho.domain.dto;

import java.time.LocalDate;

public record KuppiksenKunkkuDTO(
        Long id,
        Long gpId,
        Integer gpNo,
        LocalDate pvm,
        Long puolustajaId,
        String puolustajaNimi,
        Long haastajaId,
        String haastajaNimi,
        Long voittajaId,
        String voittajaNimi,
        boolean vyoUnohtui
) {}
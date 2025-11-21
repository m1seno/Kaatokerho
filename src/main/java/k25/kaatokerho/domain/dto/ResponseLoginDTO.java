package k25.kaatokerho.domain.dto;

import k25.kaatokerho.domain.Keilaaja;

public class ResponseLoginDTO {

    private final String token;
    private final ResponseKeilaajaDTO keilaaja;

    public ResponseLoginDTO(String token, ResponseKeilaajaDTO keilaaja) {
        this.token = token;
        this.keilaaja = keilaaja;
    }

    public String getToken() {
        return token;
    }

    public ResponseKeilaajaDTO getKeilaaja() {
        return keilaaja;
    }
}
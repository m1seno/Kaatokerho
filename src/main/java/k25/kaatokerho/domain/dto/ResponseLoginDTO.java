package k25.kaatokerho.domain.dto;

public class ResponseLoginDTO {
    private String token;

    public ResponseLoginDTO(String token) {
        this.token = token;
    }

    public String getToken() {
        return token;
    }
}

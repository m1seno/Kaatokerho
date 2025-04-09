package k25.kaatokerho.domain.dto;

import java.time.LocalDate;

public class KalenteriDTO {
    private int jarjestysnumero;
    private LocalDate pvm;
    private String keilahalli;
    private String voittaja;
    private Integer voittotulos;

    public KalenteriDTO(int jarjestysnumero, LocalDate pvm, String keilahalli, String voittaja, Integer voittotulos) {
        this.jarjestysnumero = jarjestysnumero;
        this.pvm = pvm;
        this.keilahalli = keilahalli;
        this.voittaja = voittaja;
        this.voittotulos = voittotulos;
    }

    public int getJarjestysnumero() { return jarjestysnumero; }
    public LocalDate getPvm() { return pvm; }
    public String getKeilahalli() { return keilahalli; }
    public String getVoittaja() { return voittaja; }
    public Integer getVoittotulos() { return voittotulos; }
}

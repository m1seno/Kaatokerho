package k25.kaatokerho.domain.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.NotNull;

public class UusiGpDTO {

    @NotNull
    private Integer jarjestysnumero;

    @NotNull
    private LocalDate pvm;

    @NotNull
    private Long keilahalliId;

    @NotNull
    private Boolean kultainenGp;

    @NotNull
    private Long kausiId;

    public UusiGpDTO() {
    }

    public UusiGpDTO(@NotNull Integer jarjestysnumero, @NotNull LocalDate pvm, @NotNull Long keilahalliId,
            Boolean kultainenGp, Long kausiId) {
        this.jarjestysnumero = jarjestysnumero;
        this.pvm = pvm;
        this.keilahalliId = keilahalliId;
        this.kultainenGp = kultainenGp;
        this.kausiId = kausiId;
    }

    public Integer getJarjestysnumero() {
        return jarjestysnumero;
    }

    public void setJarjestysnumero(Integer jarjestysnumero) {
        this.jarjestysnumero = jarjestysnumero;
    }

    public LocalDate getPvm() {
        return pvm;
    }

    public void setPvm(LocalDate pvm) {
        this.pvm = pvm;
    }

    public Long getKeilahalliId() {
        return keilahalliId;
    }

    public void setKeilahalliId(Long keilahalliId) {
        this.keilahalliId = keilahalliId;
    }

    public Boolean isKultainenGp() {
        return kultainenGp;
    }

    public void setKultainenGp(Boolean kultainenGp) {
        this.kultainenGp = kultainenGp;
    }

    public Long getKausiId() {
        return kausiId;
    }

    public void setKausiId(Long kausiId) {
        this.kausiId = kausiId;
    }
}

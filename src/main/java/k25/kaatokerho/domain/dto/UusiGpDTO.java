package k25.kaatokerho.domain.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.NotNull;
import k25.kaatokerho.domain.Kausi;

public class UusiGpDTO {

    @NotNull
    private Integer jarjestysnumero;

    @NotNull
    private LocalDate pvm;

    @NotNull
    private Long keilahalliId;

    @NotNull
    private boolean kultainenGp;

    @NotNull
    private Kausi kausi;

    public UusiGpDTO() {
    }

    public UusiGpDTO(@NotNull Integer jarjestysnumero, @NotNull LocalDate pvm, @NotNull Long keilahalliId,
            boolean kultainenGp, Kausi kausi) {
        this.jarjestysnumero = jarjestysnumero;
        this.pvm = pvm;
        this.keilahalliId = keilahalliId;
        this.kultainenGp = kultainenGp;
        this.kausi = kausi;
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

    public boolean isKultainenGp() {
        return kultainenGp;
    }

    public void setKultainenGp(boolean kultainenGp) {
        this.kultainenGp = kultainenGp;
    }

    public Kausi getKausi() {
        return kausi;
    }

    public void setKausi(Kausi kausi) {
        this.kausi = kausi;
    }
}

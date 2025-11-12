package k25.kaatokerho.domain.dto;

import java.time.LocalDate;

public class PaivitaGpDTO {
    private LocalDate pvm;
    private Long keilahalliId;
    private Boolean onKultainenGp;

    public PaivitaGpDTO() {
    }

    public PaivitaGpDTO(LocalDate pvm, Long keilahalliId, Boolean onKultainenGp) {
        this.pvm = pvm;
        this.keilahalliId = keilahalliId;
        this.onKultainenGp = onKultainenGp;
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

    public Boolean getOnKultainenGp() {
        return onKultainenGp;
    }

    public void setOnKultainenGp(Boolean onKultainenGp) {
        this.onKultainenGp = onKultainenGp;
    }

}

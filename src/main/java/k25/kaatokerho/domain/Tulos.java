package k25.kaatokerho.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "tulos")
public class Tulos {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "tulos_id")
    private Long tulosId;

    @ManyToOne
    @JoinColumn(name = "gp_id", nullable = false)
    private GP gp;

    @ManyToOne
    @JoinColumn(name = "keilaaja_id", nullable = false)
    private Keilaaja keilaaja;

    @Column(name = "sarja1")
    private Integer sarja1;

    @Column(name = "sarja2")
    private Integer sarja2;

    @NotNull(message = "Osallistuminen tulee merkata")
    @Column(nullable = false)
    private Boolean osallistui;

    public Tulos() {
    }

    public Tulos(GP gp, Keilaaja keilaaja, Boolean osallistui, Integer sarja1, Integer sarja2, Long tulosId) {
        this.gp = gp;
        this.keilaaja = keilaaja;
        this.osallistui = osallistui;
        this.sarja1 = sarja1;
        this.sarja2 = sarja2;
        this.tulosId = tulosId;
    }

    public Long getTulosId() {
        return tulosId;
    }

    public void setTulosId(Long tulosId) {
        this.tulosId = tulosId;
    }

    public GP getGp() {
        return gp;
    }

    public void setGp(GP gp) {
        this.gp = gp;
    }

    public Keilaaja getKeilaaja() {
        return keilaaja;
    }

    public void setKeilaaja(Keilaaja keilaaja) {
        this.keilaaja = keilaaja;
    }

    public Integer getSarja1() {
        return sarja1;
    }

    public void setSarja1(Integer sarja1) {
        this.sarja1 = sarja1;
    }

    public Integer getSarja2() {
        return sarja2;
    }

    public void setSarja2(Integer sarja2) {
        this.sarja2 = sarja2;
    }

    public Boolean getOsallistui() {
        return osallistui;
    }

    public void setOsallistui(Boolean osallistui) {
        this.osallistui = osallistui;
    }

    @Override
    public String toString() {
        return "Tulos [tulosId=" + tulosId + ", gp=" + gp + ", keilaaja=" + keilaaja + ", sarja1=" + sarja1
                + ", sarja2=" + sarja2 + ", osallistui=" + osallistui + "]";
    }
}
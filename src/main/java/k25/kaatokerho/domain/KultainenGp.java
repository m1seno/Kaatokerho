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
@Table(name = "kultainengp")
public class KultainenGp {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "kultainengp_id")
    private Long kultainenGpId;

    @ManyToOne
    @JoinColumn(name = "gp_id", nullable = false)
    private GP gp;

    @ManyToOne
    @JoinColumn(name = "keilaaja_id", nullable = false)
    private Keilaaja keilaaja;

    @NotNull(message = "Lisäpisteet ei voi olla null")
    @Column(nullable = false)
    private Integer lisapisteet;

    public KultainenGp() {
    }

    public KultainenGp(GP gp, Keilaaja keilaaja, Long kultainenGpId, Integer lisapisteet) {
        this.gp = gp;
        this.keilaaja = keilaaja;
        this.kultainenGpId = kultainenGpId;
        this.lisapisteet = lisapisteet;
    }

    public Long getKultainenGpId() {
        return kultainenGpId;
    }

    public void setKultainenGpId(Long kultainenGpId) {
        this.kultainenGpId = kultainenGpId;
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

    public Integer getLisapisteet() {
        return lisapisteet;
    }

    public void setLisapisteet(Integer lisapisteet) {
        this.lisapisteet = lisapisteet;
    }

    @Override
    public String toString() {
        return "KultainenGP [kultainenGpId=" + kultainenGpId + ", gp=" + gp + ", keilaaja=" + keilaaja
                + ", lisapisteet=" + lisapisteet + "]";
    }
}
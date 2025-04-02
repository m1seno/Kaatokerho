package k25.kaatokerho.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "Kuppiksen_Kunkku")
public class KuppiksenKunkku {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "kuppiksen_kunkku_id")
    private Long kuppiksenKunkkuId;

    @OneToOne
    @JoinColumn(name = "gp_id", nullable = false, unique = true)
    private GP gp;

    @ManyToOne
    @JoinColumn(name = "voittaja_keilaaja_id", nullable = false)
    private Keilaaja voittaja;

    @ManyToOne
    @JoinColumn(name = "haastaja_keilaaja_id", nullable = false)
    private Keilaaja haastaja;

    @NotNull(message = "Lisäpisteet ei voi olla null")
    @Column(nullable = false)
    private int lisapisteet;

    public KuppiksenKunkku() {
    }

    public KuppiksenKunkku(Long kuppiksenKunkkuId, GP gp, Keilaaja voittaja, Keilaaja haastaja, int lisapisteet) {
        this.kuppiksenKunkkuId = kuppiksenKunkkuId;
        this.gp = gp;
        this.voittaja = voittaja;
        this.haastaja = haastaja;
        this.lisapisteet = lisapisteet;
    }

    public Long getKuppiksenKunkkuId() {
        return kuppiksenKunkkuId;
    }

    public void setKuppiksenKunkkuId(Long kuppiksenKunkkuId) {
        this.kuppiksenKunkkuId = kuppiksenKunkkuId;
    }

    public GP getGp() {
        return gp;
    }

    public void setGp(GP gp) {
        this.gp = gp;
    }

    public Keilaaja getVoittaja() {
        return voittaja;
    }

    public void setVoittaja(Keilaaja voittaja) {
        this.voittaja = voittaja;
    }

    public Keilaaja getHaastaja() {
        return haastaja;
    }

    public void setHaastaja(Keilaaja haastaja) {
        this.haastaja = haastaja;
    }

    public int getLisapisteet() {
        return lisapisteet;
    }

    public void setLisapisteet(int lisapisteet) {
        this.lisapisteet = lisapisteet;
    }

    @Override
    public String toString() {
        return "KuppiksenKunkku [kuppiksenKunkkuId=" + kuppiksenKunkkuId + ", gp=" + gp + ", voittaja=" + voittaja
                + ", haastaja=" + haastaja + ", lisapisteet=" + lisapisteet + "]";
    }
}
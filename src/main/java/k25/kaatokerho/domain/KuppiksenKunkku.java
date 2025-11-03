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
@Table(name = "kuppiksenkunkku")
public class KuppiksenKunkku {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "kuppiksenkunkku_id")
    private Long kuppiksenKunkkuId;

    @OneToOne
    @JoinColumn(name = "gp_id", nullable = false, unique = true)
    private GP gp;

    @ManyToOne
    @JoinColumn(name = "puolustaja_id", nullable = false)
    private Keilaaja puolustaja;

    @ManyToOne
    @JoinColumn(name = "haastaja_id", nullable = true)
    private Keilaaja haastaja;

    @ManyToOne
    @JoinColumn(name = "voittaja_id", nullable = true)
    private Keilaaja voittaja;

    @NotNull(message = "Unohtunut vyö tulee merkata")
    @Column(name = "vyo_unohtui")
    private boolean vyoUnohtui;

    public KuppiksenKunkku() {
    }

    public KuppiksenKunkku(Long kuppiksenKunkkuId, GP gp, Keilaaja puolustaja, Keilaaja haastaja, Keilaaja voittaja, boolean vyoUnohtui) {
        this.kuppiksenKunkkuId = kuppiksenKunkkuId;
        this.gp = gp;
        this.puolustaja = puolustaja;
        this.haastaja = haastaja;
        this.voittaja = voittaja;
        this.vyoUnohtui = vyoUnohtui;
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

    public Keilaaja getPuolustaja() {
        return puolustaja;
    }

    public void setPuolustaja(Keilaaja puolustaja) {
        this.puolustaja = puolustaja;
    }

    public Keilaaja getHaastaja() {
        return haastaja;
    }

    public void setHaastaja(Keilaaja haastaja) {
        this.haastaja = haastaja;
    }

    public Keilaaja getVoittaja() {
        return voittaja;
    }

    public void setVoittaja(Keilaaja voittaja) {
        this.voittaja = voittaja;
    }

    public boolean getVyoUnohtui() {
        return vyoUnohtui;
    }

    public void setVyoUnohtui(boolean vyoUnohtui) {
        this.vyoUnohtui = vyoUnohtui;
    }

    public boolean isVyoUnohtui() {
        return vyoUnohtui;
    }

    @Override
    public String toString() {
        return "KuppiksenKunkku [kuppiksenKunkkuId=" + kuppiksenKunkkuId + ", gp=" + gp + ", puolustaja=" + puolustaja
                + ", haastaja=" + haastaja + ", voittaja=" + voittaja + ", vyoUnohtui=" + vyoUnohtui + "]";
    }

}
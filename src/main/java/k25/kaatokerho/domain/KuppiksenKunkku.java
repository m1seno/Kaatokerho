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
    @JoinColumn(name = "hallitseva_id", nullable = false)
    private Keilaaja hallitseva;

    @ManyToOne
    @JoinColumn(name = "haastaja_id", nullable = false)
    private Keilaaja haastaja;

    @NotNull(message = "Unohtunut vyö tulee merkata")
    @Column(name = "vyo_unohtui")
    private boolean vyoUnohtui;

    @NotNull(message = "Hallitsevan läsnäolo tulee merkata")
    @Column(name = "hallitseva_paikalla")
    private boolean hallitsevaPaikalla;

    @NotNull(message = "Haastajan läsnäolo tulee merkata")
    @Column(name = "haasaja_paikalla")
    private boolean haasajaPaikalla;

    public KuppiksenKunkku() {
    }

    public KuppiksenKunkku(Long kuppiksenKunkkuId, GP gp, Keilaaja hallitseva, Keilaaja haastaja, boolean vyoUnohtui, boolean hallitsevaPaikalla, boolean haasajaPaikalla) {
        this.kuppiksenKunkkuId = kuppiksenKunkkuId;
        this.gp = gp;
        this.hallitseva = hallitseva;
        this.haastaja = haastaja;
        this.vyoUnohtui = vyoUnohtui;
        this.hallitsevaPaikalla = hallitsevaPaikalla;
        this.haasajaPaikalla = haasajaPaikalla;
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

    public Keilaaja getHallitseva() {
        return hallitseva;
    }

    public void setHallitseva(Keilaaja hallitseva) {
        this.hallitseva = hallitseva;
    }

    public Keilaaja getHaastaja() {
        return haastaja;
    }

    public void setHaastaja(Keilaaja haastaja) {
        this.haastaja = haastaja;
    }

    public boolean isVyoUnohtui() {
        return vyoUnohtui;
    }

    public void setVyoUnohtui(boolean vyoUnohtui) {
        this.vyoUnohtui = vyoUnohtui;
    }

    public boolean isHallitsevaPaikalla() {
        return hallitsevaPaikalla;
    }

    public void setHallitsevaPaikalla(boolean hallitsevaPaikalla) {
        this.hallitsevaPaikalla = hallitsevaPaikalla;
    }

    public boolean isHaasajaPaikalla() {
        return haasajaPaikalla;
    }

    public void setHaasajaPaikalla(boolean haasajaPaikalla) {
        this.haasajaPaikalla = haasajaPaikalla;
    }

    @Override
    public String toString() {
        return "KuppiksenKunkku [kuppiksenKunkkuId=" + kuppiksenKunkkuId + ", gp=" + gp + ", hallitseva=" + hallitseva
                + ", haastaja=" + haastaja + ", vyoUnohtui=" + vyoUnohtui + ", hallitsevaPaikalla=" + hallitsevaPaikalla
                + ", haasajaPaikalla=" + haasajaPaikalla + "]";
    }

}
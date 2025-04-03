package k25.kaatokerho.domain;

import java.util.Date;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "gp")
public class GP {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "gp_id")
    private Long gpId;

    @ManyToOne
    @JoinColumn(name = "kausi_id", nullable = false)
    private Kausi kausi;

    @ManyToOne
    @JoinColumn(name = "keilahalli_id", nullable = false)
    private Keilahalli keilahalli;

    @NotNull(message = "Päivämäärä ei voi olla null")
    @Temporal(TemporalType.DATE)
    @Column(nullable = false)
    private Date pvm;

    @NotNull(message = "Järjestysnumero ei voi olla null")
    @Column(nullable = false)
    private Integer jarjestysnumero;

    @OneToOne(mappedBy = "gp", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<KuppiksenKunkku> kuppiksenKunkut;

    public GP() {
    }

    public GP(Long gpId, Integer jarjestysnumero, Kausi kausi, Keilahalli keilahalli, Date pvm) {
        this.gpId = gpId;
        this.jarjestysnumero = jarjestysnumero;
        this.kausi = kausi;
        this.keilahalli = keilahalli;
        this.pvm = pvm;
    }

    @Override
    public String toString() {
        return "Gp [gpId=" + gpId + ", kausi=" + kausi + ", keilahalli=" + keilahalli + ", pvm=" + pvm
                + ", jarjestysnumero=" + jarjestysnumero + "]";
    }

    public Long getGpId() {
        return gpId;
    }

    public void setGpId(Long gpId) {
        this.gpId = gpId;
    }

    public Kausi getKausi() {
        return kausi;
    }

    public void setKausi(Kausi kausi) {
        this.kausi = kausi;
    }

    public Keilahalli getKeilahalli() {
        return keilahalli;
    }

    public void setKeilahalli(Keilahalli keilahalli) {
        this.keilahalli = keilahalli;
    }

    public Date getPvm() {
        return pvm;
    }

    public void setPvm(Date pvm) {
        this.pvm = pvm;
    }

    public Integer getJarjestysnumero() {
        return jarjestysnumero;
    }

    public void setJarjestysnumero(Integer jarjestysnumero) {
        this.jarjestysnumero = jarjestysnumero;
    }

    public List<KuppiksenKunkku> getKuppiksenKunkut() {
        return kuppiksenKunkut;
    }

    public void setKuppiksenKunkut(List<KuppiksenKunkku> kuppiksenKunkut) {
        this.kuppiksenKunkut = kuppiksenKunkut;
    }

}
package k25.kaatokerho.domain;

import java.time.LocalDate;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
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
    @Column(name = "pvm", nullable = false)
    private LocalDate pvm;

    @NotNull(message = "Järjestysnumero ei voi olla null")
    @Column(name = "jarjestysnumero", nullable = false)
    private Integer jarjestysnumero;

    @NotNull(message = "Onko kultainen GP ei voi olla null")
    @Column(name = "on_kultainen_gp", nullable = false)
    private boolean onKultainenGp;

    @JsonIgnore
    @OneToOne(mappedBy = "gp", cascade = CascadeType.ALL, orphanRemoval = true)
    private KuppiksenKunkku kuppiksenKunkku;

    @JsonIgnore
    @OneToMany(mappedBy = "gp", cascade = CascadeType.ALL)
    private List<Tulos> tulokset;
    
    @JsonIgnore
    @OneToMany(mappedBy = "gp", cascade = CascadeType.ALL)
    private List<KultainenGp> kultaisetGp;

    public GP() {
    }

    public GP(Long gpId, Integer jarjestysnumero, Kausi kausi, Keilahalli keilahalli, LocalDate pvm, boolean onKultainenGp) {
        this.gpId = gpId;
        this.jarjestysnumero = jarjestysnumero;
        this.kausi = kausi;
        this.keilahalli = keilahalli;
        this.pvm = pvm;
        this.onKultainenGp = onKultainenGp;
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

    public LocalDate getPvm() {
        return pvm;
    }

    public void setPvm(LocalDate pvm) {
        this.pvm = pvm;
    }

    public Integer getJarjestysnumero() {
        return jarjestysnumero;
    }

    public boolean isOnKultainenGp() {
        return onKultainenGp;
    }

    public void setOnKultainenGp(boolean onKultainenGp) {
        this.onKultainenGp = onKultainenGp;
    }

    public void setJarjestysnumero(Integer jarjestysnumero) {
        this.jarjestysnumero = jarjestysnumero;
    }

    public KuppiksenKunkku getKuppiksenKunkku() {
        return kuppiksenKunkku;
    }

    public void setKuppiksenKunkku(KuppiksenKunkku kuppiksenKunkku) {
        this.kuppiksenKunkku = kuppiksenKunkku;
    }

    public List<Tulos> getTulokset() {
        return tulokset;
    }

    public void setTulokset(List<Tulos> tulokset) {
        this.tulokset = tulokset;
    }

    public List<KultainenGp> getKultaisetGp() {
        return kultaisetGp;
    }

    public void setKultaisetGp(List<KultainenGp> kultaisetGp) {
        this.kultaisetGp = kultaisetGp;
    }

    @Override
    public String toString() {
        return "Gp [gpId=" + gpId + ", kausi=" + kausi + ", keilahalli=" + keilahalli + ", pvm=" + pvm
                + ", jarjestysnumero=" + jarjestysnumero + "]";
    }

}
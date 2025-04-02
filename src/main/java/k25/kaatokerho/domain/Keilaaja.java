package k25.kaatokerho.domain;

import java.util.Date;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "Keilaaja")
public class Keilaaja {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "keilaaja_id")
    private Long keilaajaId;

    @NotEmpty(message = "Etunimi ei voi olla tyhjä")
    @Column(nullable = false, length = 50)
    private String etunimi;

    @NotEmpty(message = "Sukunimi ei voi olla tyhjä")
    @Column(nullable = false, length = 50)
    private String sukunimi;

    @NotNull(message = "Syntymäpäivä ei voi olla null")
    @Temporal(TemporalType.DATE)
    private Date syntymapaiva;

    @NotNull
    @Column(nullable = false)
    private Boolean aktiivijasen;

    @NotNull
    @Column(nullable = false)
    private Boolean admin;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "keilaaja")
    private List<Tulos> tulokset;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "keilaaja")
    private List<KultainenGp> kultaisetGp;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "voittaja")
    private List<KuppiksenKunkku> voittajat;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "haastaja")
    private List<KuppiksenKunkku> haastajat;

    public Keilaaja() {
    }

    public Keilaaja(Boolean admin, Boolean aktiivijasen, String etunimi, Long keilaajaId, String sukunimi,
            Date syntymapaiva) {
        this.admin = admin;
        this.aktiivijasen = aktiivijasen;
        this.etunimi = etunimi;
        this.keilaajaId = keilaajaId;
        this.sukunimi = sukunimi;
        this.syntymapaiva = syntymapaiva;
    }

    public Long getKeilaajaId() {
        return keilaajaId;
    }

    public void setKeilaajaId(Long keilaajaId) {
        this.keilaajaId = keilaajaId;
    }

    public String getEtunimi() {
        return etunimi;
    }

    public void setEtunimi(String etunimi) {
        this.etunimi = etunimi;
    }

    public String getSukunimi() {
        return sukunimi;
    }

    public void setSukunimi(String sukunimi) {
        this.sukunimi = sukunimi;
    }

    public Date getSyntymapaiva() {
        return syntymapaiva;
    }

    public void setSyntymapaiva(Date syntymapaiva) {
        this.syntymapaiva = syntymapaiva;
    }

    public Boolean getAktiivijasen() {
        return aktiivijasen;
    }

    public void setAktiivijasen(Boolean aktiivijasen) {
        this.aktiivijasen = aktiivijasen;
    }

    public Boolean getAdmin() {
        return admin;
    }

    public void setAdmin(Boolean admin) {
        this.admin = admin;
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

    public List<KuppiksenKunkku> getVoittajat() {
        return voittajat;
    }

    public void setVoittajat(List<KuppiksenKunkku> voittajat) {
        this.voittajat = voittajat;
    }

    public List<KuppiksenKunkku> getHaastajat() {
        return haastajat;
    }

    public void setHaastajat(List<KuppiksenKunkku> haastajat) {
        this.haastajat = haastajat;
    }

    @Override
    public String toString() {
        return "Keilaaja [keilaajaId=" + keilaajaId + ", etunimi=" + etunimi + ", sukunimi=" + sukunimi
                + ", syntymapaiva=" + syntymapaiva + ", aktiivijasen=" + aktiivijasen + ", admin=" + admin + "]";
    }
}
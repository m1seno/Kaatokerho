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
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "keilaaja")
public class Keilaaja {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "keilaaja_id")
    private Long keilaajaId;

    @NotEmpty(message = "Etunimi ei voi olla tyhjä")
    @Column(name = "etunimi", nullable = false, length = 50)
    private String etunimi;

    @NotEmpty(message = "Sukunimi ei voi olla tyhjä")
    @Column(name = "sukunimi", nullable = false, length = 50)
    private String sukunimi;

    @NotNull(message = "Syntymäpäivä ei voi olla null")
    @Temporal(TemporalType.DATE)
    @Column(name = "syntymapaiva", nullable = false)
    private LocalDate syntymapaiva;

    @NotNull
    @Column(name = "aktiivijasen", nullable = false)
    private Boolean aktiivijasen;

    @NotNull
    @Column(name = "admin", nullable = false)
    private Boolean admin;

    @Column(name = "kayttajanimi", length = 50)
    private String kayttajanimi;

    @Column(name = "salasana_hash", length = 60)
    private String salasanaHash;

    @JsonIgnore
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "keilaaja")
    private List<Tulos> tulokset;

    @JsonIgnore
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "keilaaja")
    private List<KultainenGp> kultaisetGp;

    @JsonIgnore
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "hallitseva")
    private List<KuppiksenKunkku> hallitsevat;

    @JsonIgnore
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "haastaja")
    private List<KuppiksenKunkku> haastajat;

    @JsonIgnore
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "keilaaja")
    private List<KeilaajaKausi> kausiTilastot;

    public Keilaaja() {
    }

    public Keilaaja(Boolean admin, Boolean aktiivijasen, String etunimi, String kayttajanimi, Long keilaajaId,
            String salasanaHash,
            String sukunimi,
            LocalDate syntymapaiva) {
        this.admin = admin;
        this.aktiivijasen = aktiivijasen;
        this.etunimi = etunimi;
        this.kayttajanimi = kayttajanimi;
        this.keilaajaId = keilaajaId;
        this.salasanaHash = salasanaHash;
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

    public LocalDate getSyntymapaiva() {
        return syntymapaiva;
    }

    public void setSyntymapaiva(LocalDate syntymapaiva) {
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

    public String getKayttajanimi() {
        return kayttajanimi;
    }

    public void setKayttajanimi(String kayttajanimi) {
        this.kayttajanimi = kayttajanimi;
    }

    public String getSalasanaHash() {
        return salasanaHash;
    }

    public void setSalasanaHash(String salasanaHash) {
        this.salasanaHash = salasanaHash;
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

    public List<KuppiksenKunkku> getHallitsevat() {
        return hallitsevat;
    }

    public void setHallitsevat(List<KuppiksenKunkku> hallitsevat) {
        this.hallitsevat = hallitsevat;
    }

    public List<KuppiksenKunkku> getHaastajat() {
        return haastajat;
    }

    public void setHaastajat(List<KuppiksenKunkku> haastajat) {
        this.haastajat = haastajat;
    }

    public List<KeilaajaKausi> getKausiTilastot() {
        return kausiTilastot;
    }

    public void setKausiTilastot(List<KeilaajaKausi> kausiTilastot) {
        this.kausiTilastot = kausiTilastot;
    }

    @Override
    public String toString() {
        return "Keilaaja [keilaajaId=" + keilaajaId + ", etunimi=" + etunimi + ", sukunimi=" + sukunimi
                + ", syntymapaiva=" + syntymapaiva + ", aktiivijasen=" + aktiivijasen + ", admin=" + admin
                + ", kayttajanimi=" + kayttajanimi
                + ", salasanaHash=" + salasanaHash + "]";
    }

}
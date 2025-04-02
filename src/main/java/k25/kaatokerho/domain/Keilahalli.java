package k25.kaatokerho.domain;

import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotEmpty;

@Entity
@Table(name = "Keilahalli")
public class Keilahalli {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "keilahalli_id")
    private Long keilahalliId;

    @NotEmpty(message = "Nimi ei voi olla tyhjä")
    @Column(nullable = false, length = 100)
    private String nimi;

    @NotEmpty(message = "Kaupunki ei voi olla tyhjä")
    @Column(nullable = false, length = 50)
    private String kaupunki;

    @NotEmpty(message = "Valtio ei voi olla tyhjä")
    @Column(nullable = false, length = 50)
    private String valtio;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "keilahalli")
    private List<GP> gpLista;

    public Keilahalli() {
    }

    public Keilahalli(String kaupunki, Long keilahalliId, String nimi, String valtio) {
        this.kaupunki = kaupunki;
        this.keilahalliId = keilahalliId;
        this.nimi = nimi;
        this.valtio = valtio;
    }

    public Long getKeilahalliId() {
        return keilahalliId;
    }

    public void setKeilahalliId(Long keilahalliId) {
        this.keilahalliId = keilahalliId;
    }

    public String getNimi() {
        return nimi;
    }

    public void setNimi(String nimi) {
        this.nimi = nimi;
    }

    public String getKaupunki() {
        return kaupunki;
    }

    public void setKaupunki(String kaupunki) {
        this.kaupunki = kaupunki;
    }

    public String getValtio() {
        return valtio;
    }

    public void setValtio(String valtio) {
        this.valtio = valtio;
    }

    public List<GP> getGpLista() {
        return gpLista;
    }

    public void setGpLista(List<GP> gpLista) {
        this.gpLista = gpLista;
    }

    @Override
    public String toString() {
        return "Keilahalli [keilahalliId=" + keilahalliId + ", nimi=" + nimi + ", kaupunki=" + kaupunki + ", valtio="
                + valtio + "]";
    }

}
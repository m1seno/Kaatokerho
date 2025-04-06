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
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "kausi")
public class Kausi {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "kausi_id")
    private Long kausiId;

    @NotEmpty(message = "Kauden nimi ei voi olla tyhjä (käytä esim. 2024-2025)")
    @Column(nullable = false, length = 20)
    private String nimi;

    @NotNull(message = "Kauden GP määrä ei voi olla null")
    @Column(name = "gpMaara", nullable = false)
    private Integer gpMaara;

    @NotNull(message = "Kauden suunniteltu GP määrä ei voi olla null")
    @Column(name = "suunniteltuGpMaara", nullable = false)
    private Integer suunniteltuGpMaara;

    @NotNull(message = "Kauden osallistujamäärä ei voi olla null")
    @Column(nullable = false)
    private Integer osallistujamaara;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "kausi")
    private List<GP> gpLista;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "kausi")
    private List<KeilaajaKausi> keilaajaTilastot;

    public Kausi() {
    }

    public Kausi(Integer gpMaara, Long kausiId, String nimi, Integer osallistujamaara, Integer suunniteltuGpMaara) {
        this.gpMaara = gpMaara;
        this.kausiId = kausiId;
        this.nimi = nimi;
        this.osallistujamaara = osallistujamaara;
        this.suunniteltuGpMaara = suunniteltuGpMaara;
    }

    public Long getKausiId() {
        return kausiId;
    }

    public void setKausiId(Long kausiId) {
        this.kausiId = kausiId;
    }

    public String getNimi() {
        return nimi;
    }

    public void setNimi(String nimi) {
        this.nimi = nimi;
    }

    public Integer getGpMaara() {
        return gpMaara;
    }

    public void setGpMaara(Integer gpMaara) {
        this.gpMaara = gpMaara;
    }

    public Integer getSuunniteltuGpMaara() {
        return suunniteltuGpMaara;
    }

    public void setSuunniteltuGpMaara(Integer suunniteltuGpMaara) {
        this.suunniteltuGpMaara = suunniteltuGpMaara;
    }

    public Integer getOsallistujamaara() {
        return osallistujamaara;
    }

    public void setOsallistujamaara(Integer osallistujamaara) {
        this.osallistujamaara = osallistujamaara;
    }

    public List<GP> getGpLista() {
        return gpLista;
    }

    public void setGpLista(List<GP> gpLista) {
        this.gpLista = gpLista;
    }

    public List<KeilaajaKausi> getKeilaajaTilastot() {
        return keilaajaTilastot;
    }

    public void setKeilaajaTilastot(List<KeilaajaKausi> keilaajaTilastot) {
        this.keilaajaTilastot = keilaajaTilastot;
    }

    @Override
    public String toString() {
        return "Kausi [kausiId=" + kausiId + ", nimi=" + nimi + ", gpMaara=" + gpMaara + ", suunniteltuGpMaara="
                + suunniteltuGpMaara + ", osallistujamaara=" + osallistujamaara + "]";
    }
}
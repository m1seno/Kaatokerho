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
@Table(name = "Keilaaja_Kausi")
public class KeilaajaKausi {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "keilaaja_kausi_id")
    private Long keilaajaKausiId;

    @ManyToOne
    @JoinColumn(name = "keilaaja_id", nullable = false)
    private Keilaaja keilaaja;

    @ManyToOne
    @JoinColumn(name = "kausi_id", nullable = false)
    private Kausi kausi;

    @NotNull(message = "parasSarja ei voi olla null")
    @Column(nullable = false)
    private Integer parasSarja;

    @NotNull(message = "huonoinSarja ei voi olla null")
    @Column(nullable = false)
    private Integer huonoinSarja;

    @NotNull(message = "kaudenPisteet ei voi olla null")
    @Column(nullable = false)
    private Integer kaudenPisteet;

    @NotNull(message = "Voitot ei voi olla null")
    @Column(nullable = false)
    private Integer voittoja;

    @NotNull(message = "Osallistumiset ei voi olla null")
    @Column(nullable = false)
    private Integer osallistumisia;

    public KeilaajaKausi() {
    }

    public KeilaajaKausi(Long keilaajaKausiId, Keilaaja keilaaja, Kausi kausi, Integer parasSarja, Integer huonoinSarja,
            Integer kaudenPisteet, Integer voittoja, Integer osallistumisia) {
        this.keilaajaKausiId = keilaajaKausiId;
        this.keilaaja = keilaaja;
        this.kausi = kausi;
        this.parasSarja = parasSarja;
        this.huonoinSarja = huonoinSarja;
        this.kaudenPisteet = kaudenPisteet;
        this.voittoja = voittoja;
        this.osallistumisia = osallistumisia;
    }

    public Long getKeilaajaKausiId() {
        return keilaajaKausiId;
    }

    public void setKeilaajaKausiId(Long keilaajaKausiId) {
        this.keilaajaKausiId = keilaajaKausiId;
    }

    public Keilaaja getKeilaaja() {
        return keilaaja;
    }

    public void setKeilaaja(Keilaaja keilaaja) {
        this.keilaaja = keilaaja;
    }

    public Kausi getKausi() {
        return kausi;
    }

    public void setKausi(Kausi kausi) {
        this.kausi = kausi;
    }

    public Integer getParasSarja() {
        return parasSarja;
    }

    public void setParasSarja(Integer parasSarja) {
        this.parasSarja = parasSarja;
    }

    public Integer getHuonoinSarja() {
        return huonoinSarja;
    }

    public void setHuonoinSarja(Integer huonoinSarja) {
        this.huonoinSarja = huonoinSarja;
    }

    public Integer getKaudenPisteet() {
        return kaudenPisteet;
    }

    public void setKaudenPisteet(Integer kaudenPisteet) {
        this.kaudenPisteet = kaudenPisteet;
    }

    public Integer getVoittoja() {
        return voittoja;
    }

    public void setVoittoja(Integer voittoja) {
        this.voittoja = voittoja;
    }

    public Integer getOsallistumisia() {
        return osallistumisia;
    }

    public void setOsallistumisia(Integer osallistumisia) {
        this.osallistumisia = osallistumisia;
    }

    @Override
    public String toString() {
        return "KeilaajaKausi [keilaajaKausiId=" + keilaajaKausiId + ", keilaaja=" + keilaaja + ", kausi=" + kausi
                + ", parasSarja=" + parasSarja + ", huonoinSarja=" + huonoinSarja + ", kaudenPisteet=" + kaudenPisteet
                + ", voittoja=" + voittoja + ", osallistumisia=" + osallistumisia + "]";
    }
}

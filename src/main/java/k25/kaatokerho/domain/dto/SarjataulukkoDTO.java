package k25.kaatokerho.domain.dto;

import java.util.List;

public class SarjataulukkoDTO {

    private int sija;
    private Long keilaajaId;
    private String nimi;
    private int gpMaara;
    private double pisteet;
    private double pisteetPerGp;
    private int gpVoitot;
    private List<Integer> gpTulokset;
    private int yhteensa;
    private double kaGp;
    private double kaSarja;

    public SarjataulukkoDTO(int sija, Long keilaajaId, String nimi, int gpMaara, double pisteet, double pisteetPerGp, int gpVoitot,
            List<Integer> gpTulokset, int yhteensa, double kaGp, double kaSarja) {
        this.sija = sija;
        this.keilaajaId = keilaajaId;
        this.nimi = nimi;
        this.gpMaara = gpMaara;
        this.pisteet = pisteet;
        this.pisteetPerGp = pisteetPerGp;
        this.gpVoitot = gpVoitot;
        this.gpTulokset = gpTulokset;
        this.yhteensa = yhteensa;
        this.kaGp = kaGp;
        this.kaSarja = kaSarja;
    }

    public int getSija() {
        return sija;
    }

    public void setSija(int sija) {
        this.sija = sija;
    }

    public Long getKeilaajaId() {
        return keilaajaId;
    }

    public String getNimi() {
        return nimi;
    }

    public int getGpMaara() {
        return gpMaara;
    }

    public double getPisteet() {
        return pisteet;
    }

    public double getPisteetPerGp() {
        return pisteetPerGp;
    }

    public int getGpVoitot() {
        return gpVoitot;
    }

    public List<Integer> getGpTulokset() {
        return gpTulokset;
    }

    public int getYhteensa() {
        return yhteensa;
    }

    public double getKaGp() {
        return kaGp;
    }

    public double getKaSarja() {
        return kaSarja;
    }
}

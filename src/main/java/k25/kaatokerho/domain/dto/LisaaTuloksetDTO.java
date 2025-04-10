package k25.kaatokerho.domain.dto;

import java.util.List;

public class LisaaTuloksetDTO {

    private Long gpId;
    private List<TulosForm> tulokset;

    public Long getGpId() { return gpId; }
    public void setGpId(Long gpId) { this.gpId = gpId; }
    public List<TulosForm> getTulokset() { return tulokset; }
    public void setTulokset(List<TulosForm> tulokset) { this.tulokset = tulokset; }

    /// Sisäkkäinen luokka tulosten käsittelyyn
    public static class TulosForm {
        private Long keilaajaId;
        private Integer sarja1;
        private Integer sarja2;

        public Long getKeilaajaId() { return keilaajaId; }
        public void setKeilaajaId(Long keilaajaId) { this.keilaajaId = keilaajaId; }
        public Integer getSarja1() { return sarja1; }
        public void setSarja1(Integer sarja1) { this.sarja1 = sarja1; }
        public Integer getSarja2() { return sarja2; }
        public void setSarja2(Integer sarja2) { this.sarja2 = sarja2; }
    }
}

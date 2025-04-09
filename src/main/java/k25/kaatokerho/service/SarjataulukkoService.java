package k25.kaatokerho.service;

import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Service;

import k25.kaatokerho.domain.GP;
import k25.kaatokerho.domain.GpRepository;
import k25.kaatokerho.domain.KeilaajaKausi;
import k25.kaatokerho.domain.KeilaajaKausiRepository;
import k25.kaatokerho.domain.dto.SarjataulukkoDTO;

@Service
public class SarjataulukkoService {

    KeilaajaKausiRepository keilaajaKausiRepository;
    GpRepository gpRepository;

    public SarjataulukkoService(KeilaajaKausiRepository keilaajaKausiRepository, GpRepository gpRepository) {
        this.keilaajaKausiRepository = keilaajaKausiRepository;
        this.gpRepository = gpRepository;
    }

    public List<SarjataulukkoDTO> haeSarjataulukko() {
        List<KeilaajaKausi> keilaajat = (List<KeilaajaKausi>) keilaajaKausiRepository.findAll();
        List<GP> gpLista = (List<GP>) gpRepository.findAll();

        return keilaajat.stream().sorted(Comparator.comparingDouble(KeilaajaKausi::getKaudenPisteet).reversed())
                .map(keilaaja -> {
                    int sija = keilaajat.indexOf(keilaaja) + 1;
                    String nimi = keilaaja.getKeilaaja().getEtunimi() + " " + keilaaja.getKeilaaja().getSukunimi();
                    int gpMaara = keilaaja.getOsallistumisia();
                    double pisteet = keilaaja.getKaudenPisteet();
                    double pisteetPerGp = keilaaja.getKaudenPisteet() / gpMaara;
                    int gpVoitot = keilaaja.getVoittoja();                    
                    List<Integer> gpTulokset = gpLista.stream()
                            .filter(gp -> gp.getTulokset().stream()
                                    .anyMatch(tulos -> tulos.getKeilaaja().getKeilaajaId() == keilaaja.getKeilaaja().getKeilaajaId()))
                            .map(gp -> gp.getTulokset().stream()
                                    .filter(tulos -> tulos.getKeilaaja().getKeilaajaId() == keilaaja.getKeilaaja().getKeilaajaId())
                                    .findFirst()
                                    .orElse(null))
                            .map(tulos -> tulos != null ? (tulos.getSarja1() + tulos.getSarja2()) : null)
                            .toList();
                    int yhteensa = gpTulokset.stream()
                            .filter(tulos -> tulos != null)
                            .mapToInt(Integer::intValue)
                            .sum();
                    double kaGp = keilaaja.getKaudenPisteet() / gpMaara;
                    double kaSarja = kaGp / 2;

                    return new SarjataulukkoDTO(sija, nimi, gpMaara, pisteet, pisteetPerGp, gpVoitot, gpTulokset,
                            yhteensa, kaGp, kaSarja);
                }).toList();
        
    }
    
}

package k25.kaatokerho.service;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.StreamSupport;

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

        // Lajitellaan pelaajat pisteiden mukaan
        List<KeilaajaKausi> jarjestetyt = keilaajat.stream()
        .sorted(Comparator.comparingDouble(KeilaajaKausi::getKaudenPisteet).reversed())
        .toList();

        //Laskuri kasvattaa numeroa joka kierroksella
        AtomicInteger sijaLaskuri = new AtomicInteger(1);

        //Annetaan arvot DTO:n muuttujille
        return jarjestetyt.stream()
                .map(keilaaja -> {
                    int sija = sijaLaskuri.getAndIncrement();
                    String nimi = keilaaja.getKeilaaja().getEtunimi() + " " + keilaaja.getKeilaaja().getSukunimi();
                    int gpMaara = keilaaja.getOsallistumisia();
                    double pisteet = keilaaja.getKaudenPisteet();
                    double pisteetPerGp = pisteet / keilaaja.getOsallistumisia();
                    int gpVoitot = keilaaja.getVoittoja();
                    // ChatGPT: Muutetaan gpTulokset List<Integer> tyyppiseksi
                    List<Integer> gpTulokset = gpLista.stream()
                            .map(gp -> gp.getTulokset().stream()
                                    .filter(t -> t.getKeilaaja().getKeilaajaId() == keilaaja.getKeilaaja()
                                            .getKeilaajaId())
                                    .findFirst()
                                    .map(tulos -> {
                                        Integer sarja1 = tulos.getSarja1();
                                        Integer sarja2 = tulos.getSarja2();
                                        return (sarja1 != null && sarja2 != null) ? sarja1 + sarja2 : null;
                                    })
                                    .orElse(null))
                            .toList();
                    int yhteensa = gpTulokset.stream()
                            .filter(tulos -> tulos != null)
                            .mapToInt(Integer::intValue)
                            .sum();
                    double kaGp = yhteensa / gpMaara;
                    double kaSarja = kaGp / 2;

                    return new SarjataulukkoDTO(sija, nimi, gpMaara, pisteet, pisteetPerGp, gpVoitot, gpTulokset,
                            yhteensa, kaGp, kaSarja);
                }).toList();

    }

    // Tehdään lista GP:n järjestysnumeroista
    public List<Integer> haeJarjestysnumerot() {
        List<Integer> gpNumerot = StreamSupport.stream(gpRepository.findAll().spliterator(), false)
                .map(GP::getJarjestysnumero)
                .sorted()
                .toList();
        return gpNumerot;
    }
}

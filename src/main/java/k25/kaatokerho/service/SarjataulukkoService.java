package k25.kaatokerho.service;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.StreamSupport;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import k25.kaatokerho.domain.GP;
import k25.kaatokerho.domain.GpRepository;
import k25.kaatokerho.domain.Kausi;
import k25.kaatokerho.domain.KeilaajaKausi;
import k25.kaatokerho.domain.KeilaajaKausiRepository;
import k25.kaatokerho.domain.dto.SarjataulukkoDTO;
import k25.kaatokerho.exception.ApiException;
import k25.kaatokerho.domain.KausiRepository;

@Service
public class SarjataulukkoService {

        private final KeilaajaKausiRepository keilaajaKausiRepository;
        private final GpRepository gpRepository;
        private final KausiRepository kausiRepository;

        public SarjataulukkoService(KeilaajaKausiRepository keilaajaKausiRepository,
                        GpRepository gpRepository,
                        KausiRepository kausiRepository) {
                this.keilaajaKausiRepository = keilaajaKausiRepository;
                this.gpRepository = gpRepository;
                this.kausiRepository = kausiRepository;
        }

        public List<SarjataulukkoDTO> haeSarjataulukko(Kausi kausi) {

                List<KeilaajaKausi> keilaajat = keilaajaKausiRepository.findByKausi(kausi);
                List<GP> gpLista = gpRepository.findByKausi(kausi);

                List<KeilaajaKausi> jarjestetyt = keilaajat.stream()
                                .sorted(Comparator.comparingDouble(KeilaajaKausi::getKaudenPisteet).reversed())
                                .toList();

                AtomicInteger sijaLaskuri = new AtomicInteger(1);

                return jarjestetyt.stream()
                                .map(keilaaja -> {
                                        int sija = sijaLaskuri.getAndIncrement();
                                        String nimi = keilaaja.getKeilaaja().getEtunimi() + " " +
                                                        keilaaja.getKeilaaja().getSukunimi();
                                        int gpMaara = keilaaja.getOsallistumisia();
                                        double pisteet = keilaaja.getKaudenPisteet();
                                        double pisteetPerGp = gpMaara > 0 ? pisteet / gpMaara : 0.0;
                                        int gpVoitot = keilaaja.getVoittoja();

                                        List<Integer> gpTulokset = gpLista.stream()
                                                        .map(gp -> gp.getTulokset().stream()
                                                                        .filter(t -> Objects.equals(
                                                                                        t.getKeilaaja().getKeilaajaId(),
                                                                                        keilaaja.getKeilaaja()
                                                                                                        .getKeilaajaId()))
                                                                        .findFirst()
                                                                        .map(tulos -> {
                                                                                Integer s1 = tulos.getSarja1();
                                                                                Integer s2 = tulos.getSarja2();
                                                                                return (s1 != null && s2 != null)
                                                                                                ? s1 + s2
                                                                                                : null;
                                                                        })
                                                                        .orElse(null))
                                                        .toList();

                                        int yhteensa = gpTulokset.stream()
                                                        .filter(Objects::nonNull)
                                                        .mapToInt(Integer::intValue)
                                                        .sum();

                                        double kaGp = gpMaara > 0 ? (double) yhteensa / gpMaara : 0.0;
                                        double kaSarja = kaGp / 2.0;

                                        return new SarjataulukkoDTO(
                                                        sija, nimi, gpMaara, pisteet, pisteetPerGp,
                                                        gpVoitot, gpTulokset, yhteensa, kaGp, kaSarja);
                                })
                                .toList();
        }

        // Sarjataulukko kuluvalle kaudelle
        public List<SarjataulukkoDTO> haeSarjataulukkoKuluvaKausi() {
                Kausi kausi = kausiRepository.findTopByOrderByKausiIdDesc();
                if (kausi == null) {
                        throw new ApiException(HttpStatus.NOT_FOUND, "Yhtään kautta ei ole vielä tallennettu");
                }
                return haeSarjataulukko(kausi);
        }

        // Sarjataulukko tietylle kaudelle
        public List<SarjataulukkoDTO> haeSarjataulukkoKausiId(Long kausiId) {
                Kausi kausi = kausiRepository.findById(kausiId)
                                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND,
                                                "Kautta ei löydy id:llä " + kausiId));
                return haeSarjataulukko(kausi);
        }

        public List<Integer> haeJarjestysnumerotNykyinenKausi() {
                Kausi kausi = kausiRepository.findTopByOrderByKausiIdDesc();
                if (kausi == null) {
                        throw new ApiException(HttpStatus.NOT_FOUND, "Yhtään kautta ei ole vielä tallennettu");
                }

                return gpRepository.findByKausi(kausi).stream()
                                .map(GP::getJarjestysnumero)
                                .sorted()
                                .toList();
        }
}

package k25.kaatokerho.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import k25.kaatokerho.domain.GP;
import k25.kaatokerho.domain.Keilaaja;
import k25.kaatokerho.domain.Tulos;
import k25.kaatokerho.domain.TulosRepository;

@Service
public class PistelaskuService {

    private final TulosRepository tulosRepository;

    public PistelaskuService(TulosRepository tulosRepository) {
        this.tulosRepository = tulosRepository;
    }

    @Transactional(readOnly = true)
    public Map<Long, Double> laskeSijoitus(GP gp) {

        // Tallennetaan keilaajien pisteet
        Map<Long, Double> keilaajaPisteet = new HashMap<>();

        // 🔹 Haetaan tulokset suoraan repositorysta, ei gp.getTulokset()
        List<Tulos> tulokset = tulosRepository.findByGp(gp).stream()
                .filter(Tulos::getOsallistui) // Vain osallistuneet
                .sorted((t1, t2) -> {
                    int summa1 = turvallinenSumma(t1);
                    int summa2 = turvallinenSumma(t2);
                    return Integer.compare(summa2, summa1); // Laskeva järjestys
                })
                .toList();

        int osallistujia = tulokset.size();
        int i = 0;

        while (i < osallistujia) {
            int nykyinenSumma = turvallinenSumma(tulokset.get(i));
            int alkuIndeksi = i;

            // Selvitetään montako keilaajaa jakaa saman tuloksen
            while (i + 1 < osallistujia &&
                    turvallinenSumma(tulokset.get(i + 1)) == nykyinenSumma) {
                i++;
            }

            int loppuIndeksi = i;
            int jaettuSijoitus = alkuIndeksi + 1;

            boolean viimeinenSija = loppuIndeksi == osallistujia - 1;
            boolean viimeinenSijaJaettu = loppuIndeksi > alkuIndeksi;
            boolean jaetaankoPisteet = osallistujia > 10 || !viimeinenSija || viimeinenSijaJaettu;

            double jaetutPisteet;

            if (viimeinenSija && viimeinenSijaJaettu && osallistujia <= 10) {
                // Jaetaan vain toiseksi viimeisen sijoituksen pisteet
                int toiseksiViimeinenSijoitus = osallistujia - 1;
                jaetutPisteet = laskeSijoituspisteet(toiseksiViimeinenSijoitus);
            } else {
                jaetutPisteet = 0;
                for (int s = jaettuSijoitus; s <= loppuIndeksi + 1; s++) {
                    jaetutPisteet += laskeSijoituspisteet(s);
                }
            }

            double keskiarvo = jaetaankoPisteet
                    ? jaetutPisteet / (loppuIndeksi - alkuIndeksi + 1)
                    : 0.0;

            // Tallennetaan pisteet jokaiselle keilaajalle
            for (int j = alkuIndeksi; j <= loppuIndeksi; j++) {
                Keilaaja k = tulokset.get(j).getKeilaaja();
                keilaajaPisteet.put(k.getKeilaajaId(), keskiarvo);
            }

            i++;
        }

        return keilaajaPisteet;
    }

    // Turvallinen summa null-arvojen varalta (vaikka osallistui=true:lla ei pitäisi olla nulleja)
    private int turvallinenSumma(Tulos t) {
        int s1 = t.getSarja1() != null ? t.getSarja1() : 0;
        int s2 = t.getSarja2() != null ? t.getSarja2() : 0;
        return s1 + s2;
    }

    // Annetaan pisteet sijoituksen mukaan
    public static double laskeSijoituspisteet(int sijoitus) {
        return switch (sijoitus) {
            case 1 -> 12;
            case 2 -> 10;
            case 3 -> 8;
            case 4 -> 7;
            case 5 -> 6;
            case 6 -> 5;
            case 7 -> 4;
            case 8 -> 3;
            case 9 -> 2;
            case 10 -> 1;
            default -> 0;
        };
    }
}
package k25.kaatokerho.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import k25.kaatokerho.domain.GP;
import k25.kaatokerho.domain.Keilaaja;
import k25.kaatokerho.domain.Tulos;

@Service
public class PistelaskuService {

    public Map<Long, Double> laskeSijoitus(GP gp) {

        // Tallennetaan keilaajien pisteet
        Map<Long, Double> keilaajaPisteet = new HashMap<>(); // Luo uusi HashMap tuloksille

        // Asetetaan keilaajien tulokset suuruusjärjestykseen
        List<Tulos> tulokset = gp.getTulokset().stream()
                .filter(Tulos::getOsallistui) // Vain osallistuneet
                .sorted((t1, t2) -> {
                    int summa1 = t1.getSarja1() + t1.getSarja2();
                    int summa2 = t2.getSarja1() + t2.getSarja2();
                    return Integer.compare(summa2, summa1); // Laskeva järjestys
                })
                .toList();

        /*
         * While-silmukka käy läpi kaikkien keilaajien tulokset ja huolehtii siitä,
         * että tasatuloksessa pisteet jaetaan (esim. (8+7)/2 = 7.5).
         * Samalla tarkistetaan, että jos keilaajia on 10 tai vähemmän,
         * viimeiseksi jäänyt ei saa pisteitä (kerhossa ei jaeta ilmaisia lounaita).
         * ChatGPT:n tekemä logiikka.
         */

        int osallistujia = tulokset.size();
        int i = 0;

        while (i < osallistujia) {
            int nykyinenSumma = tulokset.get(i).getSarja1() + tulokset.get(i).getSarja2();
            int alkuIndeksi = i;

            // Selvitetään montako keilaajaa jakaa saman tuloksen
            while (i + 1 < osallistujia &&
                    (tulokset.get(i + 1).getSarja1() + tulokset.get(i + 1).getSarja2()) == nykyinenSumma) {
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
        }; // Jos sijoitus on yli 10, ei saa pisteitä
    }
}

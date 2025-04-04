package k25.kaatokerho.service;

import java.util.List;

import k25.kaatokerho.domain.GP;
import k25.kaatokerho.domain.Keilaaja;
import k25.kaatokerho.domain.Tulos;

public class LaskeSijoituspisteet {

    public void laskeSijoitus(GP gp) {
        List<Tulos> tulokset = gp.getTulokset();
        Keilaaja[] osallistujat = new Keilaaja[tulokset.size()];

        for (Tulos tulos : tulokset) {
            Keilaaja keilaaja = tulos.getKeilaaja();
            int sarja1 = tulos.getSarja1();
            int sarja2 = tulos.getSarja2();
            int yhteensa = sarja1 + sarja2;
            
        }
    }

    // Laske sijoituspisteet GP:ssä
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

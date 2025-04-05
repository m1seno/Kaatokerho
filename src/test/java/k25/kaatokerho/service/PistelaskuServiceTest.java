package k25.kaatokerho.service;

import k25.kaatokerho.domain.GP;
import k25.kaatokerho.domain.Keilaaja;
import k25.kaatokerho.domain.Tulos;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.*;

public class PistelaskuServiceTest {

    


    // Testaa että ei-osallistuneet suodatetaan pois
    @Test
    public void testaa_osallistumisen_suodatus() {
        PistelaskuService palvelu = new PistelaskuService();
        GP gp = new GP();

        Keilaaja k1 = new Keilaaja();
        k1.setKeilaajaId(1L);
        Keilaaja k2 = new Keilaaja();
        k2.setKeilaajaId(2L);
        Keilaaja k3 = new Keilaaja();
        k3.setKeilaajaId(3L);
        Keilaaja k4 = new Keilaaja();
        k4.setKeilaajaId(4L);
        Keilaaja k5 = new Keilaaja();
        k5.setKeilaajaId(5L);
        Keilaaja k6 = new Keilaaja();
        k6.setKeilaajaId(6L);
        Keilaaja k7 = new Keilaaja();
        k7.setKeilaajaId(7L);
        Keilaaja k8 = new Keilaaja();
        k8.setKeilaajaId(8L);

        Tulos t1 = new Tulos();
        t1.setKeilaaja(k1);
        t1.setSarja1(200);
        t1.setSarja2(180);
        t1.setOsallistui(true);
        Tulos t2 = new Tulos();
        t2.setKeilaaja(k2);
        t2.setSarja1(180);
        t2.setSarja2(170);
        t2.setOsallistui(false);
        Tulos t3 = new Tulos();
        t3.setKeilaaja(k3);
        t3.setSarja1(160);
        t3.setSarja2(150);
        t3.setOsallistui(true);
        Tulos t4 = new Tulos();
        t4.setKeilaaja(k4);
        t4.setSarja1(140);
        t4.setSarja2(150);
        t4.setOsallistui(false);
        Tulos t5 = new Tulos();
        t5.setKeilaaja(k5);
        t5.setSarja1(130);
        t5.setSarja2(160);
        t5.setOsallistui(true);
        Tulos t6 = new Tulos();
        t6.setKeilaaja(k6);
        t6.setSarja1(120);
        t6.setSarja2(170);
        t6.setOsallistui(false);
        Tulos t7 = new Tulos();
        t7.setKeilaaja(k7);
        t7.setSarja1(110);
        t7.setSarja2(180);
        t7.setOsallistui(true);
        Tulos t8 = new Tulos();
        t8.setKeilaaja(k8);
        t8.setSarja1(100);
        t8.setSarja2(190);
        t8.setOsallistui(false);

        gp.setTulokset(List.of(t1, t2, t3, t4, t5, t6, t7, t8));
        Map<Long, Double> tulokset = palvelu.laskeSijoitus(gp);

        assertTrue(tulokset.containsKey(1L));
        assertTrue(tulokset.containsKey(3L));
        assertTrue(tulokset.containsKey(5L));
        assertTrue(tulokset.containsKey(7L));
        assertFalse(tulokset.containsKey(2L));
        assertFalse(tulokset.containsKey(4L));
        assertFalse(tulokset.containsKey(6L));
        assertFalse(tulokset.containsKey(8L));
    }

    // Testaa että pisteet jaetaan oikein tasatilanteessa
    @Test
    public void testaa_tasapisteet_keskiarvot() {
        PistelaskuService palvelu = new PistelaskuService();
        GP gp = new GP();

        Keilaaja k1 = new Keilaaja();
        k1.setKeilaajaId(1L);
        Keilaaja k2 = new Keilaaja();
        k2.setKeilaajaId(2L);
        Keilaaja k3 = new Keilaaja();
        k3.setKeilaajaId(3L);
        Keilaaja k4 = new Keilaaja();
        k4.setKeilaajaId(4L);
        Keilaaja k5 = new Keilaaja();
        k5.setKeilaajaId(5L);
        Keilaaja k6 = new Keilaaja();
        k6.setKeilaajaId(6L);

        Tulos t1 = new Tulos();
        t1.setKeilaaja(k1);
        t1.setSarja1(190);
        t1.setSarja2(190);
        t1.setOsallistui(true);
        Tulos t2 = new Tulos();
        t2.setKeilaaja(k2);
        t2.setSarja1(190);
        t2.setSarja2(190);
        t2.setOsallistui(true);
        Tulos t3 = new Tulos();
        t3.setKeilaaja(k3);
        t3.setSarja1(160);
        t3.setSarja2(150);
        t3.setOsallistui(true);
        Tulos t4 = new Tulos();
        t4.setKeilaaja(k4);
        t4.setSarja1(140);
        t4.setSarja2(150);
        t4.setOsallistui(true);
        Tulos t5 = new Tulos();
        t5.setKeilaaja(k5);
        t5.setSarja1(130);
        t5.setSarja2(160);
        t5.setOsallistui(true);
        Tulos t6 = new Tulos();
        t6.setKeilaaja(k6);
        t6.setSarja1(120);
        t6.setSarja2(170);
        t6.setOsallistui(true);

        gp.setTulokset(List.of(t1, t2, t3, t4, t5, t6));
        Map<Long, Double> tulokset = palvelu.laskeSijoitus(gp);

        assertEquals(11.0, tulokset.get(1L), 0.01); // (12 + 10) / 2
        assertEquals(11.0, tulokset.get(2L), 0.01);
        assertEquals(8.0, tulokset.get(3L), 0.01);
        assertEquals(6.0, tulokset.get(4L), 0.01); // (7 + 6 + 5) / 3
        assertEquals(6.0, tulokset.get(5L), 0.01);
        assertEquals(6.0, tulokset.get(6L), 0.01);
    }

    // Testaa että palautettu map sisältää keilaajaId:t ja pisteet
    @Test
    public void testaa_map_sisaltaa_keilaajaId_ja_pisteet() {
        PistelaskuService palvelu = new PistelaskuService();
        GP gp = new GP();

        Keilaaja k1 = new Keilaaja();
        k1.setKeilaajaId(101L);
        Keilaaja k2 = new Keilaaja();
        k2.setKeilaajaId(202L);

        Tulos t1 = new Tulos();
        t1.setKeilaaja(k1);
        t1.setSarja1(200);
        t1.setSarja2(180);
        t1.setOsallistui(true);
        Tulos t2 = new Tulos();
        t2.setKeilaaja(k2);
        t2.setSarja1(180);
        t2.setSarja2(170);
        t2.setOsallistui(true);

        gp.setTulokset(List.of(t1, t2));
        Map<Long, Double> tulokset = palvelu.laskeSijoitus(gp);

        assertTrue(tulokset.containsKey(101L));
        assertTrue(tulokset.containsKey(202L));
        assertEquals(12.0, tulokset.get(101L), 0.01);
        assertEquals(0.0, tulokset.get(202L), 0.01);
    }

    //Testaa, että viimeiseksi jäänyt ei saa pisteitä, jos osallistujia on 10 tai vähemmän
    @Test
    public void testaa_pisteet_10_osallistujalle_ilman_tasapeleja() {
        PistelaskuService palvelu = new PistelaskuService();
        GP gp = new GP();
        List<Tulos> tulokset = new ArrayList<>();

        for (int i = 1; i <= 10; i++) {
            Keilaaja keilaaja = new Keilaaja();
            keilaaja.setKeilaajaId((long) i);

            Tulos tulos = new Tulos();
            tulos.setKeilaaja(keilaaja);
            tulos.setOsallistui(true);
            tulos.setSarja1(100 - (i * 10));
            tulos.setSarja2(100 - (i * 10));

            tulokset.add(tulos);
        }

        gp.setTulokset(tulokset);

        Map<Long, Double> tulosMap = palvelu.laskeSijoitus(gp);

        assertEquals(12.0, tulosMap.get(1L));
        assertEquals(10.0, tulosMap.get(2L));
        assertEquals(8.0, tulosMap.get(3L));
        assertEquals(7.0, tulosMap.get(4L));
        assertEquals(6.0, tulosMap.get(5L));
        assertEquals(5.0, tulosMap.get(6L));
        assertEquals(4.0, tulosMap.get(7L));
        assertEquals(3.0, tulosMap.get(8L));
        assertEquals(2.0, tulosMap.get(9L));
        assertEquals(0.0, tulosMap.get(10L));
    }

    // Keilaajat 10, 11 ja 12 jakavat saman tuloksen, joten saavat pisteet (1 + 0 + 0) / 3 = 0.3333
    @Test
    public void testaa_yli_10_osallistujaa_ja_tasapeli_sijalla_10() {
        PistelaskuService palvelu = new PistelaskuService();
        GP gp = new GP();
        List<Tulos> tulokset = new ArrayList<>();

        for (int i = 1; i <= 12; i++) {
            Keilaaja keilaaja = new Keilaaja();
            keilaaja.setKeilaajaId((long) i);

            Tulos tulos = new Tulos();
            tulos.setKeilaaja(keilaaja);
            tulos.setOsallistui(true);
            if (i <= 9) {
                tulos.setSarja1(100 - (i * 5));
                tulos.setSarja2(100 - (i * 5));
            } else {
                tulos.setSarja1(50);
                tulos.setSarja2(50);
            }

            tulokset.add(tulos);
        }

        gp.setTulokset(tulokset);

        Map<Long, Double> tulosMap = palvelu.laskeSijoitus(gp);

        assertEquals(12.0, tulosMap.get(1L));
        assertEquals(10.0, tulosMap.get(2L));
        assertEquals(8.0, tulosMap.get(3L));
        assertEquals(7.0, tulosMap.get(4L));
        assertEquals(6.0, tulosMap.get(5L));
        assertEquals(5.0, tulosMap.get(6L));
        assertEquals(4.0, tulosMap.get(7L));
        assertEquals(3.0, tulosMap.get(8L));
        assertEquals(2.0, tulosMap.get(9L));
        assertEquals(0.3333333333333333, tulosMap.get(10L), 0.000001);
        assertEquals(0.3333333333333333, tulosMap.get(11L), 0.000001);
        assertEquals(0.3333333333333333, tulosMap.get(12L), 0.000001);
    }

    // Testaa, että useat peräkkäiset tasapelit saavat samat pisteet
    @Test
    public void testaa_useita_perakkaisia_tasapeleja() {
        PistelaskuService palvelu = new PistelaskuService();
        GP gp = new GP();
        List<Tulos> tulokset = new ArrayList<>();

        for (int i = 1; i <= 4; i++) {
            Keilaaja keilaaja = new Keilaaja();
            keilaaja.setKeilaajaId((long) i);

            Tulos tulos = new Tulos();
            tulos.setKeilaaja(keilaaja);
            tulos.setOsallistui(true);
            tulos.setSarja1(100);
            tulos.setSarja2(100);

            tulokset.add(tulos);
        }

        gp.setTulokset(tulokset);

        Map<Long, Double> tulosMap = palvelu.laskeSijoitus(gp);

        assertEquals(9.25, tulosMap.get(1L));
        assertEquals(9.25, tulosMap.get(2L));
        assertEquals(9.25, tulosMap.get(3L));
        assertEquals(9.25, tulosMap.get(4L));
    }

    // Testaa, että vain 10 ensimmäistä saa pisteitä.
    @Test
    public void testaa_yli_10_osallistujaa_viimeinen_saa_0_pistetta() {
        PistelaskuService palvelu = new PistelaskuService();
        GP gp = new GP();
        List<Tulos> tulokset = new ArrayList<>();

        for (int i = 1; i <= 11; i++) {
            Keilaaja keilaaja = new Keilaaja();
            keilaaja.setKeilaajaId((long) i);

            Tulos tulos = new Tulos();
            tulos.setKeilaaja(keilaaja);
            tulos.setOsallistui(true);
            tulos.setSarja1(100 - (i * 5));
            tulos.setSarja2(100 - (i * 5));

            tulokset.add(tulos);
        }

        gp.setTulokset(tulokset);

        Map<Long, Double> tulosMap = palvelu.laskeSijoitus(gp);

        assertEquals(12.0, tulosMap.get(1L));
        assertEquals(10.0, tulosMap.get(2L));
        assertEquals(8.0, tulosMap.get(3L));
        assertEquals(7.0, tulosMap.get(4L));
        assertEquals(6.0, tulosMap.get(5L));
        assertEquals(5.0, tulosMap.get(6L));
        assertEquals(4.0, tulosMap.get(7L));
        assertEquals(3.0, tulosMap.get(8L));
        assertEquals(2.0, tulosMap.get(9L));
        assertEquals(1.0, tulosMap.get(10L));
        assertEquals(0.0, tulosMap.get(11L)); // viimeinen ei saa pisteitä
    }

}

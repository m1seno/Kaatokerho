package k25.kaatokerho.service;

import k25.kaatokerho.domain.GP;
import k25.kaatokerho.domain.Keilaaja;
import k25.kaatokerho.domain.Tulos;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import java.util.*;

public class PistelaskuServiceTest {

    private PistelaskuService pistelaskuService;

    @BeforeEach
    public void setup() {
        pistelaskuService = new PistelaskuService();
    }

    private GP luoGPTestiTuloksilla(List<int[]> tulokset, List<Boolean> osallistuneet) {
        GP gp = new GP();
        gp.setTulokset(new ArrayList<>());

        for (int i = 0; i < tulokset.size(); i++) {
            Keilaaja k = new Keilaaja();
            k.setKeilaajaId((long) i + 1);
            Tulos t = new Tulos();
            t.setKeilaaja(k);
            t.setSarja1(tulokset.get(i)[0]);
            t.setSarja2(tulokset.get(i)[1]);
            t.setOsallistui(osallistuneet.get(i));
            gp.getTulokset().add(t);
        }
        return gp;
    }

    @Test
    public void testaaNormaaliJarjestys() {
        GP gp = luoGPTestiTuloksilla(
                List.of(
                    new int[] { 150, 150 }, new int[] { 140, 140 }, new int[] { 130, 130 }, new int[] { 120, 120 },
                    new int[] { 110, 110 }, new int[] { 100, 100 }, new int[] { 90, 90 }, new int[] { 80, 80 },
                    new int[] { 70, 70 }, new int[] { 60, 60 }, new int[] { 50, 50 }, new int[] { 40, 40 }),
                List.of(true, true, true, true, true, true, true, true, true, true, true, true));

        Map<Long, Double> tulokset = pistelaskuService.laskeSijoitus(gp);
        assertEquals(12.0, tulokset.get(1L));
        assertEquals(10.0, tulokset.get(2L));
        assertEquals(8.0, tulokset.get(3L));
        assertEquals(7.0, tulokset.get(4L));
        assertEquals(6.0, tulokset.get(5L));
        assertEquals(5.0, tulokset.get(6L));
        assertEquals(4.0, tulokset.get(7L));
        assertEquals(3.0, tulokset.get(8L));
        assertEquals(2.0, tulokset.get(9L));
        assertEquals(1.0, tulokset.get(10L));
        assertEquals(0.0, tulokset.get(11L));
        assertEquals(0.0, tulokset.get(12L));

    }

    @Test
    public void testaaTasapeliKarki() {
        GP gp = luoGPTestiTuloksilla(
                List.of(new int[] { 150, 150 }, new int[] { 150, 150 }, new int[] { 130, 130 }, new int[] { 120, 120 },
                new int[] { 110, 110 }, new int[] { 100, 100 }, new int[] { 90, 90 }, new int[] { 80, 80 },
                new int[] { 70, 70 }, new int[] { 60, 60 }, new int[] { 50, 50 }),
                List.of(true, true, true, true, true, true, true, true, true, true, true));

        Map<Long, Double> tulokset = pistelaskuService.laskeSijoitus(gp);
        assertEquals(11.0, tulokset.get(1L));
        assertEquals(11.0, tulokset.get(2L));
        assertEquals(8.0, tulokset.get(3L));
        assertEquals(7.0, tulokset.get(4L));
        assertEquals(6.0, tulokset.get(5L));
        assertEquals(5.0, tulokset.get(6L));
        assertEquals(4.0, tulokset.get(7L));
        assertEquals(3.0, tulokset.get(8L));
        assertEquals(2.0, tulokset.get(9L));
        assertEquals(1.0, tulokset.get(10L));
        assertEquals(0.0, tulokset.get(11L));
    }

    @Test
    public void testaaTasapeliViimeisenaYli10() {
        GP gp = luoGPTestiTuloksilla(
                List.of(
                        new int[] { 200, 200 }, new int[] { 195, 195 }, new int[] { 190, 190 }, new int[] { 185, 185 },
                        new int[] { 180, 180 }, new int[] { 175, 175 }, new int[] { 170, 170 }, new int[] { 165, 165 },
                        new int[] { 160, 160 }, new int[] { 100, 100 }, new int[] { 100, 100 }),
                List.of(true, true, true, true, true, true, true, true, true, true, true));

        Map<Long, Double> tulokset = pistelaskuService.laskeSijoitus(gp);
        double expected = (1 + 0) / 2.0;
        assertEquals(expected, tulokset.get(10L));
        assertEquals(expected, tulokset.get(11L));
    }

    @Test
    public void testaaTasapeliViimeisenaAlle10() {
        GP gp = luoGPTestiTuloksilla(
                List.of(
                        new int[] { 200, 200 }, new int[] { 195, 195 }, new int[] { 190, 190 }, new int[] { 185, 185 },
                        new int[] { 180, 180 }, new int[] { 175, 175 }, new int[] { 170, 170 }, new int[] { 100, 100 },
                        new int[] { 100, 100 }),
                List.of(true, true, true, true, true, true, true, true, true));

        Map<Long, Double> tulokset = pistelaskuService.laskeSijoitus(gp);
        double expected = 3.0 / 2; // vain sijan 8 pisteet jaetaan
        assertEquals(expected, tulokset.get(8L));
        assertEquals(expected, tulokset.get(9L));
    }

    @Test
    public void testaaAlle10ViimeinenEiSaa() {
        GP gp = luoGPTestiTuloksilla(
                List.of(new int[] { 200, 200 }, new int[] { 190, 190 }, new int[] { 180, 180 }),
                List.of(true, true, true));

        Map<Long, Double> tulokset = pistelaskuService.laskeSijoitus(gp);
        assertEquals(12.0, tulokset.get(1L));
        assertEquals(10.0, tulokset.get(2L));
        assertEquals(0.0, tulokset.get(3L));
    }

    @Test
    public void testaaTasapeliKeskella() {
        // 4 osallistujaa: 1. (300), 2. (250), 3. (200), 4. (200)
        // Sijat: 1, 2, 3/4 (tasapeli)
        // Pisteet: 1. -> 12, 2. -> 10, 3. ja 4. jakavat (8+7)/2 = 7.5
        GP gp = luoGPTestiTuloksilla(
                List.of(
                        new int[] { 150, 150 }, // 300, 1st
                        new int[] { 125, 125 }, // 250, 2nd
                        new int[] { 100, 100 }, // 200, 3rd tie
                        new int[] { 100, 100 },  // 200, 3rd tie
                        new int[] { 90, 90 },    // 180, 5th
                        new int[] { 80, 80 }    // 160, 6th
                ),
                List.of(true, true, true, true, true, true)
        );

        Map<Long, Double> tulokset = pistelaskuService.laskeSijoitus(gp);
        assertEquals(12.0, tulokset.get(1L));
        assertEquals(10.0, tulokset.get(2L));
        assertEquals(7.5, tulokset.get(3L));
        assertEquals(7.5, tulokset.get(4L));
        assertEquals(6.0, tulokset.get(5L));
        assertEquals(0.0, tulokset.get(6L));
    }

}
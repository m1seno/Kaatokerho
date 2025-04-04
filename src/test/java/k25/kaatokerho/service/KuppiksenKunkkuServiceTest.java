package k25.kaatokerho.service;

import k25.kaatokerho.domain.*;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class KuppiksenKunkkuServiceTest {

    @Test
    public void testaa_ensimmaisen_gp_voittaja_ja_haastaja() {
        KuppiksenKunkkuRepository kuRepo = mock(KuppiksenKunkkuRepository.class);
        TulosRepository tulosRepo = mock(TulosRepository.class);
        KuppiksenKunkkuService palvelu = new KuppiksenKunkkuService(kuRepo, tulosRepo);

        GP gp = new GP();
        gp.setJarjestysnumero(1);

        Keilaaja voittaja = new Keilaaja();
        voittaja.setEtunimi("Voittaja");

        Keilaaja haastaja = new Keilaaja();
        haastaja.setEtunimi("Haastaja");

        Tulos tulos1 = new Tulos();
        tulos1.setKeilaaja(voittaja);
        tulos1.setSarja1(200);
        tulos1.setSarja2(180);
        tulos1.setOsallistui(true);
    
        Tulos tulos2 = new Tulos();
        tulos2.setKeilaaja(haastaja);
        tulos2.setSarja1(190);
        tulos2.setSarja2(170);
        tulos2.setOsallistui(true);

        gp.setTulokset(List.of(tulos1, tulos2));

        palvelu.kasitteleKuppiksenKunkku(gp, null);

        ArgumentCaptor<KuppiksenKunkku> kaappaus = ArgumentCaptor.forClass(KuppiksenKunkku.class);
        verify(kuRepo).save(kaappaus.capture());

        KuppiksenKunkku tallennettu = kaappaus.getValue();
        assertEquals(voittaja, tallennettu.getHallitseva());
        assertEquals(haastaja, tallennettu.getHaastaja());
        assertEquals(gp, tallennettu.getGp());
    }

    @Test
    public void testaa_gp_voittaja_paikalla_ja_voittaa() {
        KuppiksenKunkkuRepository kuRepo = mock(KuppiksenKunkkuRepository.class);
        TulosRepository tulosRepo = mock(TulosRepository.class);
        KuppiksenKunkkuService palvelu = new KuppiksenKunkkuService(kuRepo, tulosRepo);

        Keilaaja edellinenVoittaja = new Keilaaja();
        edellinenVoittaja.setEtunimi("Eka");

        Keilaaja haastaja = new Keilaaja();
        haastaja.setEtunimi("Haastaja");

        GP gp = new GP();
        gp.setJarjestysnumero(2);

        KuppiksenKunkku edellinen = new KuppiksenKunkku();
        edellinen.setHallitseva(edellinenVoittaja);

        Tulos tulos1 = new Tulos();
        tulos1.setKeilaaja(edellinenVoittaja);
        tulos1.setSarja1(210);
        tulos1.setSarja2(200);
        tulos1.setOsallistui(true);
    
        Tulos tulos2 = new Tulos();
        tulos2.setKeilaaja(haastaja);
        tulos2.setSarja1(190);
        tulos2.setSarja2(180);
        tulos2.setOsallistui(true);

        gp.setTulokset(List.of(tulos1, tulos2));

        when(tulosRepo.findByGpAndKeilaaja(gp, edellinenVoittaja)).thenReturn(tulos1);
        when(tulosRepo.findByGpAndKeilaaja(gp, haastaja)).thenReturn(tulos2);

        palvelu.kasitteleKuppiksenKunkku(gp, edellinen);

        ArgumentCaptor<KuppiksenKunkku> kaappaus = ArgumentCaptor.forClass(KuppiksenKunkku.class);
        verify(kuRepo).save(kaappaus.capture());

        KuppiksenKunkku tallennettu = kaappaus.getValue();
        assertEquals(edellinenVoittaja, tallennettu.getHallitseva());
        assertEquals(haastaja, tallennettu.getHaastaja());
    }

    @Test
    public void testaa_gp_voittaja_poissa_uusi_voittaja() {
        KuppiksenKunkkuRepository kuRepo = mock(KuppiksenKunkkuRepository.class);
        TulosRepository tulosRepo = mock(TulosRepository.class);
        KuppiksenKunkkuService palvelu = new KuppiksenKunkkuService(kuRepo, tulosRepo);

        Keilaaja vanhaVoittaja = new Keilaaja();
        vanhaVoittaja.setEtunimi("VanhaVoittaja");

        Keilaaja uusiVoittaja = new Keilaaja();
        uusiVoittaja.setEtunimi("Uusi");

        Keilaaja toinen = new Keilaaja();
        toinen.setEtunimi("Toinen");

        GP gp = new GP();
        gp.setJarjestysnumero(2);

        KuppiksenKunkku edellinen = new KuppiksenKunkku();
        edellinen.setHallitseva(vanhaVoittaja);

        Tulos tulos1 = new Tulos();
        tulos1.setKeilaaja(uusiVoittaja);
        tulos1.setSarja1(210);
        tulos1.setSarja2(200);
        tulos1.setOsallistui(true);
    
        Tulos tulos2 = new Tulos();
        tulos2.setKeilaaja(toinen);
        tulos2.setSarja1(190);
        tulos2.setSarja2(180);
        tulos2.setOsallistui(true);

        gp.setTulokset(List.of(tulos1, tulos2));

        palvelu.kasitteleKuppiksenKunkku(gp, edellinen);

        ArgumentCaptor<KuppiksenKunkku> kaappaus = ArgumentCaptor.forClass(KuppiksenKunkku.class);
        verify(kuRepo).save(kaappaus.capture());

        KuppiksenKunkku tallennettu = kaappaus.getValue();
        assertEquals(uusiVoittaja, tallennettu.getHallitseva());
        assertNull(tallennettu.getHaastaja());
    }

    @Test
    public void testaa_tyhjien_tulosten_kasittely() {
        KuppiksenKunkkuRepository kuRepo = mock(KuppiksenKunkkuRepository.class);
        TulosRepository tulosRepo = mock(TulosRepository.class);
        KuppiksenKunkkuService palvelu = new KuppiksenKunkkuService(kuRepo, tulosRepo);

        GP gp = new GP();
        gp.setJarjestysnumero(1);
        gp.setTulokset(Collections.emptyList());

        assertThrows(IndexOutOfBoundsException.class, () -> {
            palvelu.kasitteleKuppiksenKunkku(gp, null);
        });
    }
}
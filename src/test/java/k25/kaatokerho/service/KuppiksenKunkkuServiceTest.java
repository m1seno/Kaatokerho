package k25.kaatokerho.service;

import k25.kaatokerho.domain.*;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class KuppiksenKunkkuServiceTest {

    // First GP scenario correctly identifies winner and challenger
    @Test
    public void test_first_gp_identifies_winner_and_challenger() {
        // Arrange
        KuppiksenKunkkuRepository mockKkRepo = mock(KuppiksenKunkkuRepository.class);
        TulosRepository mockTulosRepo = mock(TulosRepository.class);
        KuppiksenKunkkuService service = new KuppiksenKunkkuService(mockKkRepo, mockTulosRepo);

        GP gp = new GP();
        gp.setJarjestysnumero(1);

        Keilaaja winner = new Keilaaja();
        winner.setEtunimi("Winner");

        Keilaaja challenger = new Keilaaja();
        challenger.setEtunimi("Challenger");

        Tulos winnerResult = new Tulos();
        winnerResult.setKeilaaja(winner);
        winnerResult.setSarja1(200);
        winnerResult.setSarja2(180);
        winnerResult.setOsallistui(true);

        Tulos challengerResult = new Tulos();
        challengerResult.setKeilaaja(challenger);
        challengerResult.setSarja1(190);
        challengerResult.setSarja2(170);
        challengerResult.setOsallistui(true);

        List<Tulos> tulokset = List.of(winnerResult, challengerResult);
        gp.setTulokset(tulokset);

        // Act
        service.kasitteleKuppiksenKunkku(gp, null, false);

        // Assert
        ArgumentCaptor<KuppiksenKunkku> kkCaptor = ArgumentCaptor.forClass(KuppiksenKunkku.class);
        verify(mockKkRepo).save(kkCaptor.capture());

        KuppiksenKunkku savedKk = kkCaptor.getValue();
        assertEquals(winner, savedKk.getHallitseva());
        assertEquals(challenger, savedKk.getHaastaja());
        assertEquals(gp, savedKk.getGp());
        assertFalse(savedKk.getVyoUnohtui());

        List<Keilaaja> haastajat = service.getHaastajalista(gp);
        assertEquals(1, haastajat.size());
        assertEquals(challenger, haastajat.get(0));
    }

    // Subsequent GP with present champion and challenger results in correct winner
    // selection
    @Test
    public void test_subsequent_gp_with_present_champion_and_challenger() {
        // Arrange
        KuppiksenKunkkuRepository mockKkRepo = mock(KuppiksenKunkkuRepository.class);
        TulosRepository mockTulosRepo = mock(TulosRepository.class);
        KuppiksenKunkkuService service = new KuppiksenKunkkuService(mockKkRepo, mockTulosRepo);

        GP previousGp = new GP();
        previousGp.setJarjestysnumero(1);

        GP currentGp = new GP();
        currentGp.setJarjestysnumero(2);

        Keilaaja champion = new Keilaaja();
        champion.setEtunimi("Champion");

        Keilaaja challenger = new Keilaaja();
        challenger.setEtunimi("Challenger");

        KuppiksenKunkku previous = new KuppiksenKunkku();
        previous.setGp(previousGp);
        previous.setHallitseva(champion);

        Tulos championResult = new Tulos();
        championResult.setKeilaaja(champion);
        championResult.setSarja1(180);
        championResult.setSarja2(190);
        championResult.setOsallistui(true);

        Tulos challengerResult = new Tulos();
        challengerResult.setKeilaaja(challenger);
        challengerResult.setSarja1(200);
        challengerResult.setSarja2(210);
        challengerResult.setOsallistui(true);

        List<Tulos> tulokset = List.of(championResult, challengerResult);
        currentGp.setTulokset(tulokset);

        when(mockTulosRepo.findByGpAndKeilaaja(currentGp, champion)).thenReturn(championResult);
        when(mockTulosRepo.findByGpAndKeilaaja(currentGp, challenger)).thenReturn(challengerResult);

        // Act
        service.kasitteleKuppiksenKunkku(currentGp, previous, false);

        // Assert
        ArgumentCaptor<KuppiksenKunkku> kkCaptor = ArgumentCaptor.forClass(KuppiksenKunkku.class);
        verify(mockKkRepo).save(kkCaptor.capture());

        KuppiksenKunkku savedKk = kkCaptor.getValue();
        assertEquals(challenger, savedKk.getHallitseva());
        assertEquals(champion, savedKk.getHaastaja());
        assertEquals(currentGp, savedKk.getGp());
        assertFalse(savedKk.getVyoUnohtui());
    }

    // Champion retains title when no challenger is present
    @Test
    public void test_champion_retains_title_when_no_challenger_present() {
        // Arrange
        KuppiksenKunkkuRepository mockKkRepo = mock(KuppiksenKunkkuRepository.class);
        TulosRepository mockTulosRepo = mock(TulosRepository.class);
        KuppiksenKunkkuService service = new KuppiksenKunkkuService(mockKkRepo, mockTulosRepo);

        GP previousGp = new GP();
        previousGp.setJarjestysnumero(1);

        GP currentGp = new GP();
        currentGp.setJarjestysnumero(2);

        Keilaaja champion = new Keilaaja();
        champion.setEtunimi("Champion");

        KuppiksenKunkku previous = new KuppiksenKunkku();
        previous.setGp(previousGp);
        previous.setHallitseva(champion);

        Tulos championResult = new Tulos();
        championResult.setKeilaaja(champion);
        championResult.setSarja1(180);
        championResult.setSarja2(190);
        championResult.setOsallistui(true);

        List<Tulos> tulokset = List.of(championResult);
        currentGp.setTulokset(tulokset);

        // Act
        service.kasitteleKuppiksenKunkku(currentGp, previous, false);

        // Assert
        ArgumentCaptor<KuppiksenKunkku> kkCaptor = ArgumentCaptor.forClass(KuppiksenKunkku.class);
        verify(mockKkRepo).save(kkCaptor.capture());

        KuppiksenKunkku savedKk = kkCaptor.getValue();
        assertEquals(champion, savedKk.getHallitseva());
        assertNull(savedKk.getHaastaja());
        assertEquals(currentGp, savedKk.getGp());
        assertFalse(savedKk.getVyoUnohtui());
    }

    // Challenger becomes champion when champion is absent
    @Test
    public void test_challenger_becomes_champion_when_champion_absent() {
        // Arrange
        KuppiksenKunkkuRepository mockKkRepo = mock(KuppiksenKunkkuRepository.class);
        TulosRepository mockTulosRepo = mock(TulosRepository.class);
        KuppiksenKunkkuService service = new KuppiksenKunkkuService(mockKkRepo, mockTulosRepo);

        GP previousGp = new GP();
        previousGp.setJarjestysnumero(1);

        GP currentGp = new GP();
        currentGp.setJarjestysnumero(2);

        Keilaaja champion = new Keilaaja();
        champion.setEtunimi("Champion");

        Keilaaja challenger = new Keilaaja();
        challenger.setEtunimi("Challenger");

        KuppiksenKunkku previous = new KuppiksenKunkku();
        previous.setGp(previousGp);
        previous.setHallitseva(champion);

        Tulos challengerResult = new Tulos();
        challengerResult.setKeilaaja(challenger);
        challengerResult.setSarja1(200);
        challengerResult.setSarja2(210);
        challengerResult.setOsallistui(true);

        List<Tulos> tulokset = List.of(challengerResult);
        currentGp.setTulokset(tulokset);

        // Act
        service.kasitteleKuppiksenKunkku(currentGp, previous, false);

        // Assert
        ArgumentCaptor<KuppiksenKunkku> kkCaptor = ArgumentCaptor.forClass(KuppiksenKunkku.class);
        verify(mockKkRepo).save(kkCaptor.capture());

        KuppiksenKunkku savedKk = kkCaptor.getValue();
        assertEquals(challenger, savedKk.getHallitseva());
        assertNull(savedKk.getHaastaja());
        assertEquals(currentGp, savedKk.getGp());
        assertFalse(savedKk.getVyoUnohtui());
    }

    // Challenger becomes champion when belt is forgotten
    @Test
    public void test_challenger_becomes_champion_when_belt_forgotten() {
        // Arrange
        KuppiksenKunkkuRepository mockKkRepo = mock(KuppiksenKunkkuRepository.class);
        TulosRepository mockTulosRepo = mock(TulosRepository.class);
        KuppiksenKunkkuService service = new KuppiksenKunkkuService(mockKkRepo, mockTulosRepo);

        GP previousGp = new GP();
        previousGp.setJarjestysnumero(1);

        GP currentGp = new GP();
        currentGp.setJarjestysnumero(2);

        Keilaaja champion = new Keilaaja();
        champion.setEtunimi("Champion");

        Keilaaja challenger = new Keilaaja();
        challenger.setEtunimi("Challenger");

        KuppiksenKunkku previous = new KuppiksenKunkku();
        previous.setGp(previousGp);
        previous.setHallitseva(champion);

        Tulos championResult = new Tulos();
        championResult.setKeilaaja(champion);
        championResult.setSarja1(200);
        championResult.setSarja2(210);
        championResult.setOsallistui(true);

        Tulos challengerResult = new Tulos();
        challengerResult.setKeilaaja(challenger);
        challengerResult.setSarja1(180);
        challengerResult.setSarja2(190);
        challengerResult.setOsallistui(true);

        List<Tulos> tulokset = List.of(championResult, challengerResult);
        currentGp.setTulokset(tulokset);

        // Act
        service.kasitteleKuppiksenKunkku(currentGp, previous, true);

        // Assert
        ArgumentCaptor<KuppiksenKunkku> kkCaptor = ArgumentCaptor.forClass(KuppiksenKunkku.class);
        verify(mockKkRepo).save(kkCaptor.capture());

        KuppiksenKunkku savedKk = kkCaptor.getValue();
        assertEquals(challenger, savedKk.getHallitseva());
        assertNull(savedKk.getHaastaja());
        assertEquals(currentGp, savedKk.getGp());
        assertTrue(savedKk.getVyoUnohtui());
    }

    // Haastajalistat is correctly populated for each GP
    /*@Test
    public void test_haastajalistat_correctly_populated() {
        // Arrange
        KuppiksenKunkkuRepository mockKkRepo = mock(KuppiksenKunkkuRepository.class);
        TulosRepository mockTulosRepo = mock(TulosRepository.class);
        KuppiksenKunkkuService service = new KuppiksenKunkkuService(mockKkRepo, mockTulosRepo);

        GP gp1 = new GP();
        gp1.setJarjestysnumero(1);

        GP gp2 = new GP();
        gp2.setJarjestysnumero(2);

        Keilaaja keilaaja1 = new Keilaaja();
        keilaaja1.setEtunimi("Keilaaja1");

        Keilaaja keilaaja2 = new Keilaaja();
        keilaaja2.setEtunimi("Keilaaja2");

        Keilaaja keilaaja3 = new Keilaaja();
        keilaaja3.setEtunimi("Keilaaja3");

        // First GP results
        Tulos tulos1Gp1 = new Tulos();
        tulos1Gp1.setKeilaaja(keilaaja1);
        tulos1Gp1.setSarja1(200);
        tulos1Gp1.setSarja2(190);
        tulos1Gp1.setOsallistui(true);

        Tulos tulos2Gp1 = new Tulos();
        tulos2Gp1.setKeilaaja(keilaaja2);
        tulos2Gp1.setSarja1(180);
        tulos2Gp1.setSarja2(170);
        tulos2Gp1.setOsallistui(true);

        List<Tulos> tuloksetGp1 = List.of(tulos1Gp1, tulos2Gp1);
        gp1.setTulokset(tuloksetGp1);
    }*/

    // Tie in best series between players triggers IllegalStateException
    @Test
    public void test_tie_in_first_gp_triggers_exception() {
        // Arrange
        KuppiksenKunkkuRepository mockKkRepo = mock(KuppiksenKunkkuRepository.class);
        TulosRepository mockTulosRepo = mock(TulosRepository.class);
        KuppiksenKunkkuService service = new KuppiksenKunkkuService(mockKkRepo, mockTulosRepo);

        GP gp = new GP();
        gp.setJarjestysnumero(1); // Simuloidaan ensimmäistä GP:tä

        Keilaaja player1 = new Keilaaja();
        player1.setEtunimi("Player1");

        Keilaaja player2 = new Keilaaja();
        player2.setEtunimi("Player2");

        Tulos result1 = new Tulos();
        result1.setKeilaaja(player1);
        result1.setSarja1(200);
        result1.setSarja2(180);
        result1.setOsallistui(true);

        Tulos result2 = new Tulos();
        result2.setKeilaaja(player2);
        result2.setSarja1(200);
        result2.setSarja2(180);
        result2.setOsallistui(true);

        gp.setTulokset(List.of(result1, result2));

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> {
            service.kasitteleKuppiksenKunkku(gp, null, false);
        });
    }

    // KaudenKaksintaistelut list is properly updated after each GP
    @Test
    public void test_kauden_kaksintaistelut_updated_after_each_gp() {
        // Arrange
        KuppiksenKunkkuRepository mockKkRepo = mock(KuppiksenKunkkuRepository.class);
        TulosRepository mockTulosRepo = mock(TulosRepository.class);
        KuppiksenKunkkuService service = new KuppiksenKunkkuService(mockKkRepo, mockTulosRepo);

        // --- GP1 setup ---
        GP gp1 = new GP();
        gp1.setJarjestysnumero(1);

        Keilaaja winner1 = new Keilaaja();
        winner1.setEtunimi("Winner1");

        Keilaaja challenger1 = new Keilaaja();
        challenger1.setEtunimi("Challenger1");

        Tulos winnerResult1 = new Tulos();
        winnerResult1.setKeilaaja(winner1);
        winnerResult1.setSarja1(210);
        winnerResult1.setSarja2(190);
        winnerResult1.setOsallistui(true);

        Tulos challengerResult1 = new Tulos();
        challengerResult1.setKeilaaja(challenger1);
        challengerResult1.setSarja1(200);
        challengerResult1.setSarja2(180);
        challengerResult1.setOsallistui(true);

        gp1.setTulokset(List.of(winnerResult1, challengerResult1));

        // --- GP2 setup ---
        GP gp2 = new GP();
        gp2.setJarjestysnumero(2);

        Keilaaja winner2 = new Keilaaja();
        winner2.setEtunimi("Winner2");

        Keilaaja challenger2 = new Keilaaja();
        challenger2.setEtunimi("Challenger2");

        Tulos winnerResult2 = new Tulos();
        winnerResult2.setKeilaaja(winner2);
        winnerResult2.setSarja1(220);
        winnerResult2.setSarja2(200);
        winnerResult2.setOsallistui(true);

        Tulos challengerResult2 = new Tulos();
        challengerResult2.setKeilaaja(challenger2);
        challengerResult2.setSarja1(210);
        challengerResult2.setSarja2(190);
        challengerResult2.setOsallistui(true);

        gp2.setTulokset(List.of(winnerResult2, challengerResult2));

        // Simuloidaan tietokannan palauttamia tuloksia
        when(mockTulosRepo.findByGpAndKeilaaja(gp2, winner1)).thenReturn(winnerResult2);
        when(mockTulosRepo.findByGpAndKeilaaja(gp2, challenger2)).thenReturn(challengerResult2);

        // Act
        service.kasitteleKuppiksenKunkku(gp1, null, false);

        // Simuloidaan edellisen GP:n kuppis-tilanne käsin
        KuppiksenKunkku edellinen = new KuppiksenKunkku();
        edellinen.setGp(gp1);
        edellinen.setHallitseva(winner1);
        edellinen.setHaastaja(challenger1);
        edellinen.setVyoUnohtui(false);

        service.kasitteleKuppiksenKunkku(gp2, edellinen, false);

        // Assert
        List<KuppiksenKunkku> kaksintaistelut = service.getKaudenKaksintaistelut();
        assertEquals(2, kaksintaistelut.size());

        KuppiksenKunkku firstBattle = kaksintaistelut.get(0);
        assertEquals(winner1, firstBattle.getHallitseva());
        assertEquals(challenger1, firstBattle.getHaastaja());
        assertEquals(gp1, firstBattle.getGp());

        KuppiksenKunkku secondBattle = kaksintaistelut.get(1);
        assertEquals(winner2, secondBattle.getHallitseva());
        assertEquals(challenger2, secondBattle.getHaastaja());
        assertEquals(gp2, secondBattle.getGp());
    }

    // Tie in both best and worst series triggers IllegalStateException
    @Test
    public void test_tie_in_best_and_worst_series_throws_exception() {
        // Arrange
        KuppiksenKunkkuRepository mockKkRepo = mock(KuppiksenKunkkuRepository.class);
        TulosRepository mockTulosRepo = mock(TulosRepository.class);
        KuppiksenKunkkuService service = new KuppiksenKunkkuService(mockKkRepo, mockTulosRepo);

        GP gp = new GP();
        gp.setJarjestysnumero(2);

        Keilaaja keilaaja1 = new Keilaaja();
        keilaaja1.setEtunimi("Keilaaja1");

        Keilaaja keilaaja2 = new Keilaaja();
        keilaaja2.setEtunimi("Keilaaja2");

        Tulos tulos1 = new Tulos();
        tulos1.setKeilaaja(keilaaja1);
        tulos1.setSarja1(200);
        tulos1.setSarja2(180);
        tulos1.setOsallistui(true);

        Tulos tulos2 = new Tulos();
        tulos2.setKeilaaja(keilaaja2);
        tulos2.setSarja1(200);
        tulos2.setSarja2(180);
        tulos2.setOsallistui(true);

        List<Tulos> tulokset = List.of(tulos1, tulos2);
        gp.setTulokset(tulokset);

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> {
            service.kasitteleKuppiksenKunkku(gp, null, false);
        });
    }

    // Empty results list handling
    @Test
    public void test_empty_results_list() {
        // Arrange
        KuppiksenKunkkuRepository mockKkRepo = mock(KuppiksenKunkkuRepository.class);
        TulosRepository mockTulosRepo = mock(TulosRepository.class);
        KuppiksenKunkkuService service = new KuppiksenKunkkuService(mockKkRepo, mockTulosRepo);

        GP gp = new GP();
        gp.setJarjestysnumero(2);
        gp.setTulokset(Collections.emptyList());

        KuppiksenKunkku edellinen = new KuppiksenKunkku();
        Keilaaja edellinenVoittaja = new Keilaaja();
        edellinenVoittaja.setEtunimi("Previous Winner");
        edellinen.setHallitseva(edellinenVoittaja);

        // Act
        service.kasitteleKuppiksenKunkku(gp, edellinen, false);

        // Assert
        ArgumentCaptor<KuppiksenKunkku> kkCaptor = ArgumentCaptor.forClass(KuppiksenKunkku.class);
        verify(mockKkRepo).save(kkCaptor.capture());

        KuppiksenKunkku savedKk = kkCaptor.getValue();
        assertEquals(edellinenVoittaja, savedKk.getHallitseva());
        assertNull(savedKk.getHaastaja());
        assertEquals(gp, savedKk.getGp());
        assertFalse(savedKk.getVyoUnohtui());

        List<Keilaaja> haastajat = service.getHaastajalista(gp);
        assertTrue(haastajat.isEmpty());
    }

    // No challenger available (all potential challengers absent)
    @Test
    public void test_no_challenger_available() {
        // Arrange
        KuppiksenKunkkuRepository mockKkRepo = mock(KuppiksenKunkkuRepository.class);
        TulosRepository mockTulosRepo = mock(TulosRepository.class);
        KuppiksenKunkkuService service = new KuppiksenKunkkuService(mockKkRepo, mockTulosRepo);

        GP gp = new GP();
        gp.setJarjestysnumero(2);

        Keilaaja previousWinner = new Keilaaja();
        previousWinner.setEtunimi("Previous Winner");

        KuppiksenKunkku previousKk = new KuppiksenKunkku();
        previousKk.setHallitseva(previousWinner);

        Tulos previousWinnerResult = new Tulos();
        previousWinnerResult.setKeilaaja(previousWinner);
        previousWinnerResult.setSarja1(200);
        previousWinnerResult.setSarja2(180);
        previousWinnerResult.setOsallistui(true);

        List<Tulos> tulokset = List.of(previousWinnerResult);
        gp.setTulokset(tulokset);

        // Act
        service.kasitteleKuppiksenKunkku(gp, previousKk, false);

        // Assert
        ArgumentCaptor<KuppiksenKunkku> kkCaptor = ArgumentCaptor.forClass(KuppiksenKunkku.class);
        verify(mockKkRepo).save(kkCaptor.capture());

        KuppiksenKunkku savedKk = kkCaptor.getValue();
        assertEquals(previousWinner, savedKk.getHallitseva());
        assertNull(savedKk.getHaastaja());
        assertEquals(gp, savedKk.getGp());
        assertFalse(savedKk.getVyoUnohtui());

        List<Keilaaja> haastajat = service.getHaastajalista(gp);
        assertTrue(haastajat.isEmpty());
    }

    // First GP with only one participant
    @Test
    public void test_first_gp_with_single_participant() {
        // Arrange
        KuppiksenKunkkuRepository mockKkRepo = mock(KuppiksenKunkkuRepository.class);
        TulosRepository mockTulosRepo = mock(TulosRepository.class);
        KuppiksenKunkkuService service = new KuppiksenKunkkuService(mockKkRepo, mockTulosRepo);

        GP gp = new GP();
        gp.setJarjestysnumero(1);

        Keilaaja singleParticipant = new Keilaaja();
        singleParticipant.setEtunimi("SingleParticipant");

        Tulos singleResult = new Tulos();
        singleResult.setKeilaaja(singleParticipant);
        singleResult.setSarja1(150);
        singleResult.setSarja2(140);
        singleResult.setOsallistui(true);

        List<Tulos> tulokset = List.of(singleResult);
        gp.setTulokset(tulokset);

        // Act
        service.kasitteleKuppiksenKunkku(gp, null, false);

        // Assert
        ArgumentCaptor<KuppiksenKunkku> kkCaptor = ArgumentCaptor.forClass(KuppiksenKunkku.class);
        verify(mockKkRepo).save(kkCaptor.capture());

        KuppiksenKunkku savedKk = kkCaptor.getValue();
        assertEquals(singleParticipant, savedKk.getHallitseva());
        assertNull(savedKk.getHaastaja());
        assertEquals(gp, savedKk.getGp());
        assertFalse(savedKk.getVyoUnohtui());

        List<Keilaaja> haastajat = service.getHaastajalista(gp);
        assertEquals(1, haastajat.size());
        assertNull(haastajat.get(0));
    }

    // Null edellinen parameter handling
    @Test
    public void test_null_edellinen_parameter_handling() {
        // Arrange
        KuppiksenKunkkuRepository mockKkRepo = mock(KuppiksenKunkkuRepository.class);
        TulosRepository mockTulosRepo = mock(TulosRepository.class);
        KuppiksenKunkkuService service = new KuppiksenKunkkuService(mockKkRepo, mockTulosRepo);

        GP gp = new GP();
        gp.setJarjestysnumero(2);

        Keilaaja winner = new Keilaaja();
        winner.setEtunimi("Winner");

        Keilaaja challenger = new Keilaaja();
        challenger.setEtunimi("Challenger");

        Tulos winnerResult = new Tulos();
        winnerResult.setKeilaaja(winner);
        winnerResult.setSarja1(210);
        winnerResult.setSarja2(190);
        winnerResult.setOsallistui(true);

        Tulos challengerResult = new Tulos();
        challengerResult.setKeilaaja(challenger);
        challengerResult.setSarja1(200);
        challengerResult.setSarja2(180);
        challengerResult.setOsallistui(true);

        List<Tulos> tulokset = List.of(winnerResult, challengerResult);
        gp.setTulokset(tulokset);

        // Act
        service.kasitteleKuppiksenKunkku(gp, null, false);

        // Assert
        ArgumentCaptor<KuppiksenKunkku> kkCaptor = ArgumentCaptor.forClass(KuppiksenKunkku.class);
        verify(mockKkRepo).save(kkCaptor.capture());

        KuppiksenKunkku savedKk = kkCaptor.getValue();
        assertEquals(winner, savedKk.getHallitseva());
        assertEquals(challenger, savedKk.getHaastaja());
        assertEquals(gp, savedKk.getGp());
        assertFalse(savedKk.getVyoUnohtui());

        List<Keilaaja> haastajat = service.getHaastajalista(gp);
        assertEquals(1, haastajat.size());
        assertEquals(challenger, haastajat.get(0));
    }

    // Filtering of participants who didn't attend (osallistui=false)
    @Test
    public void test_filtering_non_participants() {
        // Arrange
        KuppiksenKunkkuRepository mockKkRepo = mock(KuppiksenKunkkuRepository.class);
        TulosRepository mockTulosRepo = mock(TulosRepository.class);
        KuppiksenKunkkuService service = new KuppiksenKunkkuService(mockKkRepo, mockTulosRepo);

        GP gp = new GP();
        gp.setJarjestysnumero(2);

        Keilaaja participant = new Keilaaja();
        participant.setEtunimi("Participant");

        Keilaaja nonParticipant = new Keilaaja();
        nonParticipant.setEtunimi("NonParticipant");

        Tulos participantResult = new Tulos();
        participantResult.setKeilaaja(participant);
        participantResult.setSarja1(150);
        participantResult.setSarja2(160);
        participantResult.setOsallistui(true);

        Tulos nonParticipantResult = new Tulos();
        nonParticipantResult.setKeilaaja(nonParticipant);
        nonParticipantResult.setSarja1(140);
        nonParticipantResult.setSarja2(130);
        nonParticipantResult.setOsallistui(false);

        List<Tulos> tulokset = List.of(participantResult, nonParticipantResult);
        gp.setTulokset(tulokset);

        // Act
        service.kasitteleKuppiksenKunkku(gp, null, false);

        // Assert
        List<Keilaaja> haastajat = service.getHaastajalista(gp);
        assertEquals(1, haastajat.size());
        assertEquals(participant, haastajat.get(0));
    }

    // When best series are tied, worst series is used as tiebreaker
    @Test
    public void test_tiebreaker_with_worst_series() {
        // Arrange
        KuppiksenKunkkuRepository mockKkRepo = mock(KuppiksenKunkkuRepository.class);
        TulosRepository mockTulosRepo = mock(TulosRepository.class);
        KuppiksenKunkkuService service = new KuppiksenKunkkuService(mockKkRepo, mockTulosRepo);

        GP gp = new GP();
        gp.setJarjestysnumero(2);

        Keilaaja keilaaja1 = new Keilaaja();
        keilaaja1.setEtunimi("Keilaaja1");

        Keilaaja keilaaja2 = new Keilaaja();
        keilaaja2.setEtunimi("Keilaaja2");

        Tulos tulos1 = new Tulos();
        tulos1.setKeilaaja(keilaaja1);
        tulos1.setSarja1(200);
        tulos1.setSarja2(180);
        tulos1.setOsallistui(true);

        Tulos tulos2 = new Tulos();
        tulos2.setKeilaaja(keilaaja2);
        tulos2.setSarja1(200);
        tulos2.setSarja2(170);
        tulos2.setOsallistui(true);

        List<Tulos> tulokset = List.of(tulos1, tulos2);
        gp.setTulokset(tulokset);

        KuppiksenKunkku edellinen = new KuppiksenKunkku();
        edellinen.setHallitseva(keilaaja1);

        // Act
        service.kasitteleKuppiksenKunkku(gp, edellinen, false);

        // Assert
        ArgumentCaptor<KuppiksenKunkku> kkCaptor = ArgumentCaptor.forClass(KuppiksenKunkku.class);
        verify(mockKkRepo).save(kkCaptor.capture());

        KuppiksenKunkku savedKk = kkCaptor.getValue();
        assertEquals(keilaaja1, savedKk.getHallitseva());
        assertEquals(keilaaja2, savedKk.getHaastaja());
        assertEquals(gp, savedKk.getGp());
        assertFalse(savedKk.getVyoUnohtui());
    }

    // VyoUnohtui flag properly affects champion selection logic
    @Test
    public void test_vyo_unohtui_affects_champion_selection() {
        // Arrange
        KuppiksenKunkkuRepository mockKkRepo = mock(KuppiksenKunkkuRepository.class);
        TulosRepository mockTulosRepo = mock(TulosRepository.class);
        KuppiksenKunkkuService service = new KuppiksenKunkkuService(mockKkRepo, mockTulosRepo);

        GP gp = new GP();
        gp.setJarjestysnumero(2);

        Keilaaja previousChampion = new Keilaaja();
        previousChampion.setEtunimi("Previous Champion");

        Keilaaja challenger1 = new Keilaaja();
        challenger1.setEtunimi("Challenger 1");

        Tulos previousChampionResult = new Tulos();
        previousChampionResult.setKeilaaja(previousChampion);
        previousChampionResult.setSarja1(210);
        previousChampionResult.setSarja2(190);
        previousChampionResult.setOsallistui(true);

        Tulos challenger1Result = new Tulos();
        challenger1Result.setKeilaaja(challenger1);
        challenger1Result.setSarja1(200);
        challenger1Result.setSarja2(180);
        challenger1Result.setOsallistui(true);

        List<Tulos> tulokset = List.of(previousChampionResult, challenger1Result);
        gp.setTulokset(tulokset);

        KuppiksenKunkku edellinen = new KuppiksenKunkku();
        edellinen.setHallitseva(previousChampion);

        // Act
        service.kasitteleKuppiksenKunkku(gp, edellinen, true);

        // Assert
        ArgumentCaptor<KuppiksenKunkku> kkCaptor = ArgumentCaptor.forClass(KuppiksenKunkku.class);
        verify(mockKkRepo).save(kkCaptor.capture());

        KuppiksenKunkku savedKk = kkCaptor.getValue();
        assertEquals(challenger1, savedKk.getHallitseva());
        assertEquals(previousChampion, savedKk.getHaastaja());
        assertEquals(gp, savedKk.getGp());
        assertTrue(savedKk.getVyoUnohtui());

        List<Keilaaja> haastajat = service.getHaastajalista(gp);
        assertEquals(1, haastajat.size());
        assertEquals(challenger1, haastajat.get(0));
    }

    // Repository interactions correctly save KuppiksenKunkku objects
    @Test
    public void test_repository_saves_kuppiksenkunkku_correctly() {
        // Arrange
        KuppiksenKunkkuRepository mockKkRepo = mock(KuppiksenKunkkuRepository.class);
        TulosRepository mockTulosRepo = mock(TulosRepository.class);
        KuppiksenKunkkuService service = new KuppiksenKunkkuService(mockKkRepo, mockTulosRepo);

        GP gp = new GP();
        gp.setJarjestysnumero(2);

        Keilaaja previousWinner = new Keilaaja();
        previousWinner.setEtunimi("Previous Winner");

        Keilaaja newChallenger = new Keilaaja();
        newChallenger.setEtunimi("New Challenger");

        Tulos previousWinnerResult = new Tulos();
        previousWinnerResult.setKeilaaja(previousWinner);
        previousWinnerResult.setSarja1(210);
        previousWinnerResult.setSarja2(190);
        previousWinnerResult.setOsallistui(true);

        Tulos newChallengerResult = new Tulos();
        newChallengerResult.setKeilaaja(newChallenger);
        newChallengerResult.setSarja1(200);
        newChallengerResult.setSarja2(180);
        newChallengerResult.setOsallistui(true);

        List<Tulos> tulokset = List.of(previousWinnerResult, newChallengerResult);
        gp.setTulokset(tulokset);

        KuppiksenKunkku edellinen = new KuppiksenKunkku();
        edellinen.setHallitseva(previousWinner);

        // Act
        service.kasitteleKuppiksenKunkku(gp, edellinen, false);

        // Assert
        ArgumentCaptor<KuppiksenKunkku> kkCaptor = ArgumentCaptor.forClass(KuppiksenKunkku.class);
        verify(mockKkRepo).save(kkCaptor.capture());

        KuppiksenKunkku savedKk = kkCaptor.getValue();
        assertEquals(previousWinner, savedKk.getHallitseva());
        assertEquals(newChallenger, savedKk.getHaastaja());
        assertEquals(gp, savedKk.getGp());
        assertFalse(savedKk.getVyoUnohtui());
    }

    // Challenger selection prioritizes player with highest best series
    @Test
    public void test_challenger_selection_prioritizes_highest_best_series() {
        // Arrange
        KuppiksenKunkkuRepository mockKkRepo = mock(KuppiksenKunkkuRepository.class);
        TulosRepository mockTulosRepo = mock(TulosRepository.class);
        KuppiksenKunkkuService service = new KuppiksenKunkkuService(mockKkRepo, mockTulosRepo);

        GP gp = new GP();
        gp.setJarjestysnumero(2);

        Keilaaja previousWinner = new Keilaaja();
        previousWinner.setEtunimi("Previous Winner");

        Keilaaja challenger1 = new Keilaaja();
        challenger1.setEtunimi("Challenger 1");

        Keilaaja challenger2 = new Keilaaja();
        challenger2.setEtunimi("Challenger 2");

        Tulos previousWinnerResult = new Tulos();
        previousWinnerResult.setKeilaaja(previousWinner);
        previousWinnerResult.setSarja1(180);
        previousWinnerResult.setSarja2(170);
        previousWinnerResult.setOsallistui(true);

        Tulos challenger1Result = new Tulos();
        challenger1Result.setKeilaaja(challenger1);
        challenger1Result.setSarja1(200);
        challenger1Result.setSarja2(190);
        challenger1Result.setOsallistui(true);

        Tulos challenger2Result = new Tulos();
        challenger2Result.setKeilaaja(challenger2);
        challenger2Result.setSarja1(195);
        challenger2Result.setSarja2(185);
        challenger2Result.setOsallistui(true);

        List<Tulos> tulokset = List.of(previousWinnerResult, challenger1Result, challenger2Result);
        gp.setTulokset(tulokset);

        KuppiksenKunkku edellinen = new KuppiksenKunkku();
        edellinen.setHallitseva(previousWinner);

        // Act
        service.kasitteleKuppiksenKunkku(gp, edellinen, false);

        // Assert
        ArgumentCaptor<KuppiksenKunkku> kkCaptor = ArgumentCaptor.forClass(KuppiksenKunkku.class);
        verify(mockKkRepo).save(kkCaptor.capture());

        KuppiksenKunkku savedKk = kkCaptor.getValue();
        assertEquals(previousWinner, savedKk.getHallitseva());
        assertEquals(challenger1, savedKk.getHaastaja());
        assertEquals(gp, savedKk.getGp());
        assertFalse(savedKk.getVyoUnohtui());

        List<Keilaaja> haastajat = service.getHaastajalista(gp);
        assertEquals(2, haastajat.size());
        assertEquals(challenger1, haastajat.get(0));
    }
}
package k25.kaatokerho.service;

import java.util.ArrayList;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import k25.kaatokerho.domain.GP;
import k25.kaatokerho.domain.Kausi;
import k25.kaatokerho.domain.Keilaaja;
import k25.kaatokerho.domain.KeilaajaKausi;
import k25.kaatokerho.domain.KeilaajaKausiRepository;
import k25.kaatokerho.domain.KultainenGp;
import k25.kaatokerho.domain.KultainenGpRepository;
import k25.kaatokerho.domain.Tulos;

@ExtendWith(MockitoExtension.class)
class KultainenGpServiceTest {

    @Mock
    private KultainenGpRepository kultainenGpRepository;

    @Mock
    private KeilaajaKausiRepository keilaajaKausiRepository;

    @InjectMocks
    private KultainenGpService service;

    private Kausi kausi;
    private GP gp;

    private Keilaaja a;
    private Keilaaja b;
    private Keilaaja c;
    private Keilaaja d;

    @BeforeEach
    void setup() {
        kausi = new Kausi();
        kausi.setKausiId(2025L);
        kausi.setNimi("2024-2025");

        gp = new GP();
        gp.setGpId(6L);
        gp.setKausi(kausi);
        gp.setOnKultainenGp(true);
        gp.setTulokset(new ArrayList<>());

        a = keilaaja(1L, "A", "Alpha");
        b = keilaaja(2L, "B", "Beta");
        c = keilaaja(3L, "C", "Gamma");
        d = keilaaja(4L, "D", "Delta");
    }

    // --------- APUT: POJO-rakentajat ---------

    private Keilaaja keilaaja(Long id, String etu, String suku) {
        Keilaaja k = new Keilaaja();
        k.setKeilaajaId(id);
        k.setEtunimi(etu);
        k.setSukunimi(suku);
        return k;
    }

    private Tulos tulos(Keilaaja k, Integer s1, Integer s2, boolean osallistui) {
        Tulos t = new Tulos();
        t.setKeilaaja(k);
        t.setSarja1(s1);
        t.setSarja2(s2);
        t.setOsallistui(osallistui);
        return t;
    }

    private KeilaajaKausi kk(Keilaaja k, Kausi kausi, Integer paras, Integer huonoin) {
        KeilaajaKausi x = new KeilaajaKausi();
        x.setKeilaaja(k);
        x.setKausi(kausi);
        x.setParasSarja(paras);
        x.setHuonoinSarja(huonoin);
        x.setKaudenPisteet(0.0);
        x.setVoittoja(0);
        x.setOsallistumisia(0);
        return x;
    }

    // ========== TESTIT ==========

    @Test
    void eiKultainenGp_eiTeeMitaan() {
        gp.setOnKultainenGp(false);
        gp.getTulokset().add(tulos(a, 200, 190, true));

        service.kultainenPistelasku(gp);

        verifyNoInteractions(kultainenGpRepository);
        verifyNoInteractions(keilaajaKausiRepository);
    }

    @Test
    void poistaaVanhatJaTallentaaVainOsallistujat_yksiEiOsallistunut() {
        gp.getTulokset().add(tulos(a, 200, 180, true));
        gp.getTulokset().add(tulos(b, 190, 170, true));
        gp.getTulokset().add(tulos(c, null, null, false)); // EI TALLENNU
        gp.getTulokset().add(tulos(d, 150, 160, true));

        when(keilaajaKausiRepository.findByKeilaajaAndKausi(any(), eq(kausi)))
                .thenReturn(Optional.of(kk(a, kausi, 180, 120)))
                .thenReturn(Optional.of(kk(b, kausi, 170, 120)))
                .thenReturn(Optional.empty()) // c -> ei väliä, koska ei osallistunut
                .thenReturn(Optional.of(kk(d, kausi, 155, 110)));

        service.kultainenPistelasku(gp);

        InOrder inOrder = inOrder(kultainenGpRepository);
        inOrder.verify(kultainenGpRepository).deleteByGp(gp);
        ArgumentCaptor<KultainenGp> cap = ArgumentCaptor.forClass(KultainenGp.class);
        verify(kultainenGpRepository, times(3)).save(cap.capture());

        // vain A,B,D
        assertThat(cap.getAllValues())
                .extracting(k -> k.getKeilaaja().getKeilaajaId())
                .containsExactlyInAnyOrder(1L, 2L, 4L);

        // bonuspisteiden summa osallistujille rajojen puitteissa
        for (KultainenGp row : cap.getAllValues()) {
            assertThat(row.getLisapisteet()).isBetween(-2.0, 2.0);
        }
    }

    @Test
    void henkkohtBonus_YlitysTaiSivuaminen_tuottaaPlusYhden() {
        // A ylittää oman kauden parhaan -> +1
        gp.getTulokset().add(tulos(a, 210, 190, true)); // paras 210
        gp.getTulokset().add(tulos(b, 170, 160, true));

        when(keilaajaKausiRepository.findByKeilaajaAndKausi(eq(a), eq(kausi)))
                .thenReturn(Optional.of(kk(a, kausi, 200, 130))); // A:n paras ennen GP:tä = 200
        when(keilaajaKausiRepository.findByKeilaajaAndKausi(eq(b), eq(kausi)))
                .thenReturn(Optional.of(kk(b, kausi, 180, 140)));

        service.kultainenPistelasku(gp);

        ArgumentCaptor<KultainenGp> cap = ArgumentCaptor.forClass(KultainenGp.class);
        verify(kultainenGpRepository, times(2)).save(cap.capture());

        KultainenGp aRow = cap.getAllValues().stream()
                .filter(x -> x.getKeilaaja().getKeilaajaId().equals(1L)).findFirst().orElseThrow();

        // A saa vähintään +1 henk.koht. kategoriasta (paras ylitetty/sivuttu)
        assertThat(aRow.getLisapisteet()).isGreaterThanOrEqualTo(1.0);
    }

    @Test
void parasJaHuonoinJaetaanSamallaEriPelaajille() {
    // A & B jakavat GP:n parhaan (220) -> +0.5 kummallekin
    // C & D jakavat GP:n huonoimman (90)  -> -0.5 kummallekin
    gp.getTulokset().add(tulos(a, 220, 150, true));
    gp.getTulokset().add(tulos(b, 220, 140, true));
    gp.getTulokset().add(tulos(c, 180,  90, true));
    gp.getTulokset().add(tulos(d, 170,  90, true));

    // Ei henkilökohtaisia bonuksia/miinuksia tässä testissä
    when(keilaajaKausiRepository.findByKeilaajaAndKausi(any(), eq(kausi)))
            .thenAnswer(inv -> Optional.empty());

    service.kultainenPistelasku(gp);

    ArgumentCaptor<KultainenGp> cap = ArgumentCaptor.forClass(KultainenGp.class);
    verify(kultainenGpRepository, times(4)).save(cap.capture());

    double aPts = cap.getAllValues().stream().filter(x -> x.getKeilaaja().getKeilaajaId().equals(1L)).findFirst().orElseThrow().getLisapisteet();
    double bPts = cap.getAllValues().stream().filter(x -> x.getKeilaaja().getKeilaajaId().equals(2L)).findFirst().orElseThrow().getLisapisteet();
    double cPts = cap.getAllValues().stream().filter(x -> x.getKeilaaja().getKeilaajaId().equals(3L)).findFirst().orElseThrow().getLisapisteet();
    double dPts = cap.getAllValues().stream().filter(x -> x.getKeilaaja().getKeilaajaId().equals(4L)).findFirst().orElseThrow().getLisapisteet();

    assertThat(aPts).isEqualTo(0.5);
    assertThat(bPts).isEqualTo(0.5);
    assertThat(cPts).isEqualTo(-0.5);
    assertThat(dPts).isEqualTo(-0.5);
}

@Test
void parasJaHuonoinJaetaanSamallaSamoillePelaajille_nettoNolla() {
    // A & B: paras 220 (jaettu) => +0.5, mutta myös huonoin 100 (jaettu) => -0.5 → netto 0
    gp.getTulokset().add(tulos(a, 220, 100, true));
    gp.getTulokset().add(tulos(b, 220, 100, true));
    gp.getTulokset().add(tulos(c, 210, 150, true));

    // Ei henkilökohtaisia vaikutuksia
    when(keilaajaKausiRepository.findByKeilaajaAndKausi(any(), eq(kausi)))
            .thenAnswer(inv -> Optional.empty());

    service.kultainenPistelasku(gp);

    ArgumentCaptor<KultainenGp> cap = ArgumentCaptor.forClass(KultainenGp.class);
    verify(kultainenGpRepository, times(3)).save(cap.capture());

    double aPts = cap.getAllValues().stream().filter(x -> x.getKeilaaja().getKeilaajaId().equals(1L)).findFirst().orElseThrow().getLisapisteet();
    double bPts = cap.getAllValues().stream().filter(x -> x.getKeilaaja().getKeilaajaId().equals(2L)).findFirst().orElseThrow().getLisapisteet();

    assertThat(aPts).isEqualTo(0.0);
    assertThat(bPts).isEqualTo(0.0);
}

    @Test
    void huonoinSarjaMiinusJaetaanTasapelissa() {
        // C ja D heittävät GP:n huonoimman sarjan (sama arvo) -> jaetaan -1
        gp.getTulokset().add(tulos(a, 200, 200, true));
        gp.getTulokset().add(tulos(b, 190, 190, true));
        gp.getTulokset().add(tulos(c, 100, 150, true)); // huonoin 100
        gp.getTulokset().add(tulos(d, 100, 160, true));

        when(keilaajaKausiRepository.findByKeilaajaAndKausi(eq(a), eq(kausi)))
                .thenReturn(Optional.of(kk(a, kausi, 180, 120)));
        when(keilaajaKausiRepository.findByKeilaajaAndKausi(eq(b), eq(kausi)))
                .thenReturn(Optional.of(kk(b, kausi, 180, 120)));
        when(keilaajaKausiRepository.findByKeilaajaAndKausi(eq(c), eq(kausi)))
                .thenReturn(Optional.of(kk(c, kausi, 180, 120))); // henk.koht. huonoin ei täyty -> vain overall - jaon puitteissa
        when(keilaajaKausiRepository.findByKeilaajaAndKausi(eq(d), eq(kausi)))
                .thenReturn(Optional.of(kk(d, kausi, 180, 120)));

        service.kultainenPistelasku(gp);

        ArgumentCaptor<KultainenGp> cap = ArgumentCaptor.forClass(KultainenGp.class);
        verify(kultainenGpRepository, times(4)).save(cap.capture());

        double cPts = cap.getAllValues().stream()
                .filter(x -> x.getKeilaaja().getKeilaajaId().equals(3L)).findFirst().orElseThrow().getLisapisteet();
        double dPts = cap.getAllValues().stream()
                .filter(x -> x.getKeilaaja().getKeilaajaId().equals(4L)).findFirst().orElseThrow().getLisapisteet();

        assertThat(cPts).isLessThanOrEqualTo(-0.5);
        assertThat(dPts).isLessThanOrEqualTo(-0.5);
    }

    @Test
    void ensimmainenGp_EiHenkkohtBonustaMuttaVoiSaadaParasTaiHuonoin() {
        // A: ensimmäinen GP (ei KeilaajaKausi-riviä) -> ei henk.koht. bonusta, mutta voi saada paras/huonoin -kategoriasta
        gp.getTulokset().add(tulos(a, 220, 210, true)); // paras koko GP:stä
        gp.getTulokset().add(tulos(b, 150, 140, true));

        when(keilaajaKausiRepository.findByKeilaajaAndKausi(eq(a), eq(kausi)))
                .thenReturn(Optional.empty()); // ensimmäinen GP
        when(keilaajaKausiRepository.findByKeilaajaAndKausi(eq(b), eq(kausi)))
                .thenReturn(Optional.of(kk(b, kausi, 180, 120)));

        service.kultainenPistelasku(gp);

        ArgumentCaptor<KultainenGp> cap = ArgumentCaptor.forClass(KultainenGp.class);
        verify(kultainenGpRepository, times(2)).save(cap.capture());

        double aPts = cap.getAllValues().stream()
                .filter(x -> x.getKeilaaja().getKeilaajaId().equals(1L)).findFirst().orElseThrow().getLisapisteet();

        // A:n pisteen tulee tulla vain "paras sarja" -kategoriasta (1.0), ei henk.koht.
        assertThat(aPts).isEqualTo(1.0);
    }

    @Test
    void pisteetCappautuvatPlusMiinusKaksi() {
        // A: Ylittää henk.koht. parhaan (+1) ja voittaa myös GP:n parhaan sarjan (+1) => yhteensä 2 (cap 2)
        gp.getTulokset().add(tulos(a, 230, 220, true)); // paras 230
        gp.getTulokset().add(tulos(b, 200, 190, true));
        gp.getTulokset().add(tulos(c, 120, 110, true)); // huonoin 110 (C saa -1)
        gp.getTulokset().add(tulos(d, 180, 170, true));

        when(keilaajaKausiRepository.findByKeilaajaAndKausi(eq(a), eq(kausi)))
                .thenReturn(Optional.of(kk(a, kausi, 200, 130))); // A ylittää parhaan -> +1
        when(keilaajaKausiRepository.findByKeilaajaAndKausi(eq(b), eq(kausi)))
                .thenReturn(Optional.of(kk(b, kausi, 200, 130)));
        when(keilaajaKausiRepository.findByKeilaajaAndKausi(eq(c), eq(kausi)))
                .thenReturn(Optional.of(kk(c, kausi, 210, 130))); // alittaa huonoimman -> -1
        when(keilaajaKausiRepository.findByKeilaajaAndKausi(eq(d), eq(kausi)))
                .thenReturn(Optional.of(kk(d, kausi, 210, 130)));

        service.kultainenPistelasku(gp);

        ArgumentCaptor<KultainenGp> cap = ArgumentCaptor.forClass(KultainenGp.class);
        verify(kultainenGpRepository, times(4)).save(cap.capture());

        double aPts = cap.getAllValues().stream()
                .filter(x -> x.getKeilaaja().getKeilaajaId().equals(1L)).findFirst().orElseThrow().getLisapisteet();
        double cPts = cap.getAllValues().stream()
                .filter(x -> x.getKeilaaja().getKeilaajaId().equals(3L)).findFirst().orElseThrow().getLisapisteet();

        assertThat(aPts).isEqualTo(2.0);   // cap +2
        assertThat(cPts).isEqualTo(-2.0); // cap -2
    }
}

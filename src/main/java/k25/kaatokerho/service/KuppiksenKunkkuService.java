package k25.kaatokerho.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import k25.kaatokerho.domain.GP;
import k25.kaatokerho.domain.Keilaaja;
import k25.kaatokerho.domain.KuppiksenKunkku;
import k25.kaatokerho.domain.KuppiksenKunkkuRepository;
import k25.kaatokerho.domain.Tulos;

@Service
public class KuppiksenKunkkuService {

    private final KuppiksenKunkkuRepository kuppiksenKunkkuRepository;

    public KuppiksenKunkkuService(KuppiksenKunkkuRepository kuppiksenKunkkuRepository) {
        this.kuppiksenKunkkuRepository = kuppiksenKunkkuRepository;
    }

    private final List<KuppiksenKunkku> kaudenKaksintaistelut = new ArrayList<>();
    private final Map<GP, List<Keilaaja>> haastajalistat = new HashMap<>();

    @Transactional
    public void kasitteleKuppiksenKunkku(GP gp, KuppiksenKunkku edellinen, boolean vyoUnohtui) {
        // 1) Vain osallistuneet, joilla molemmat sarjat != null → vältetään NPE:t
        List<Tulos> osallistuneet = gp.getTulokset().stream()
                .filter(Tulos::getOsallistui)
                .filter(t -> t.getSarja1() != null && t.getSarja2() != null)
                .toList();

        if (osallistuneet.isEmpty()) {
            // Ei ottelua, ei haastajalistaa tallennettavaksi
            return;
        }

        // 2) Ranking: paras DESC, huonompi DESC, deterministinen tiebreak keilaajaId ASC
        List<Tulos> ranking = new ArrayList<>(osallistuneet);
        ranking.sort(Comparator
                .comparingInt((Tulos t) -> Math.max(t.getSarja1(), t.getSarja2())).reversed()
                .thenComparingInt(t -> Math.min(t.getSarja1(), t.getSarja2())).reversed()
                .thenComparing(t -> Optional.ofNullable(t.getKeilaaja().getKeilaajaId()).orElse(Long.MAX_VALUE)));

        // 3) Ensimmäinen GP (ei edellistä): vyö +1 parhaalle, haastaja = ranking[1] jos on
        if (gp.getJarjestysnumero() == 1 || edellinen == null) {
            Tulos voittajaT = ranking.get(0);
            Keilaaja voittaja = voittajaT.getKeilaaja();

            Keilaaja haastaja = null;
            if (ranking.size() > 1) {
                haastaja = ranking.get(1).getKeilaaja();
            }

            // Talteen listan näyttöä varten — älä tee List.of(null)
            haastajalistat.put(gp, haastaja != null ? List.of(haastaja) : Collections.emptyList());

            // Tallennus (ensimmäisessä GP:ssä vyö annetaan automaattisesti voittajalle)
            lisaaKaksintaistelu(gp, voittaja, haastaja, false);
            return;
        }

        // 4) Muut GP:t
        Keilaaja haltijaPrev = edellinen.getHallitseva();

        // Haastajalista = ranking ilman haltijaa
        List<Keilaaja> haastajat = ranking.stream()
                .map(Tulos::getKeilaaja)
                .filter(k -> !k.equals(haltijaPrev))
                .toList();
        haastajalistat.put(gp, haastajat);

        boolean haltijaPaikalla = osallistuneet.stream()
                .anyMatch(t -> t.getKeilaaja().equals(haltijaPrev));

        Keilaaja lopullinenHaastaja = null;
        Keilaaja uusiHaltija = haltijaPrev;

        if (!haltijaPaikalla || vyoUnohtui) {
            // 4a) Haltija poissa TAI vyö unohtui -> vyö vapautuu ranking #1:lle (haastajalistan kärki)
            if (!haastajat.isEmpty()) {
                uusiHaltija = haastajat.get(0);
                // vyö siirtyy ja ottelun voittaja = uusiHaltija; lisäpisteen kirjaaminen tapahtuu muualla
            }
            lisaaKaksintaistelu(gp, uusiHaltija, null, vyoUnohtui);
            return;
        }

        // 4b) Haltija paikalla ja vyö mukana → valitaan korkein läsnäoleva haastaja (ei haltija)
        Optional<Tulos> challengerOpt = ranking.stream()
                .map(Tulos::getKeilaaja)
                .filter(k -> !k.equals(haltijaPrev))
                .findFirst()
                .flatMap(k -> osallistuneet.stream().filter(t -> t.getKeilaaja().equals(k)).findFirst());

        if (challengerOpt.isEmpty()) {
            // Ei haastajaa -> haltija pitää vyön
            lisaaKaksintaistelu(gp, uusiHaltija, null, false);
            return;
        }

        Tulos haltijaT = osallistuneet.stream()
                .filter(t -> t.getKeilaaja().equals(haltijaPrev))
                .findFirst().orElseThrow(); // oltava, koska paikalla
        Tulos haastajaT = challengerOpt.get();
        lopullinenHaastaja = haastajaT.getKeilaaja();

        // 5) Ottelun voittaja: paras sarja → tiebreak huonompi sarja → täydellinen tasapeli => virhe (testivaatimus)
        int hBest = Math.max(haltijaT.getSarja1(), haltijaT.getSarja2());
        int hWorst = Math.min(haltijaT.getSarja1(), haltijaT.getSarja2());
        int cBest = Math.max(haastajaT.getSarja1(), haastajaT.getSarja2());
        int cWorst = Math.min(haastajaT.getSarja1(), haastajaT.getSarja2());

        if (cBest > hBest || (cBest == hBest && cWorst > hWorst)) {
            uusiHaltija = lopullinenHaastaja;
        } else if (cBest == hBest && cWorst == hWorst) {
            throw new IllegalStateException("Tasapeli: käyttäjän täytyy valita voittaja käsin.");
        } // muuten haltija pitää vyön

        lisaaKaksintaistelu(gp, uusiHaltija, lopullinenHaastaja, false);
    }

    // Tallennus ja kauden listan päivitys
    private void lisaaKaksintaistelu(GP gp, Keilaaja voittaja, Keilaaja haastaja, boolean vyoUnohtui) {
        KuppiksenKunkku kk = new KuppiksenKunkku();
        kk.setGp(gp);
        kk.setHallitseva(voittaja);
        kk.setHaastaja(haastaja);
        kk.setVyoUnohtui(vyoUnohtui);
        kuppiksenKunkkuRepository.save(kk);
        kaudenKaksintaistelut.add(kk);
    }

    public List<KuppiksenKunkku> getKaudenKaksintaistelut() {
        return kaudenKaksintaistelut;
    }

    public List<Keilaaja> getHaastajalista(GP gp) {
        return haastajalistat.getOrDefault(gp, Collections.emptyList());
    }
}
package k25.kaatokerho.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import k25.kaatokerho.domain.GP;
import k25.kaatokerho.domain.Keilaaja;
import k25.kaatokerho.domain.KeilaajaKausiRepository;
import k25.kaatokerho.domain.KultainenGp;
import k25.kaatokerho.domain.KultainenGpRepository;
import k25.kaatokerho.domain.Tulos;

@Service
public class KultainenGpService {

    private final KultainenGpRepository kultainenGpRepository;
    private final KeilaajaKausiRepository keilaajaKausiRepository;

    public KultainenGpService(KultainenGpRepository kultainenGpRepository,
                              KeilaajaKausiRepository keilaajaKausiRepository) {
        this.kultainenGpRepository = kultainenGpRepository;
        this.keilaajaKausiRepository = keilaajaKausiRepository;
    }

    // kertymä yhtä GP:tä kohti
    private final Map<Long, Double> keilaajaPisteetMap = new HashMap<>();

    @Transactional
    public void kultainenPistelasku(GP gp) {
        if (!gp.isOnKultainenGp()) return;

        keilaajaPisteetMap.clear();

        // käsittele vain osallistujat
        List<Tulos> osallistuneet = gp.getTulokset().stream()
                .filter(Tulos::getOsallistui)
                .toList();
        if (osallistuneet.isEmpty()) {
            // idempotentti: siivoa mahdolliset vanhat
            kultainenGpRepository.deleteByGp(gp);
            return;
        }

        // Kaikkien osallistujien sarjat (molemmat sarjat, ei-null)
        List<Integer> kaikkiSarjat = osallistuneet.stream()
                .flatMap(t -> Stream.of(t.getSarja1(), t.getSarja2()))
                .filter(Objects::nonNull)
                .toList();

        int parasGp = kaikkiSarjat.stream().max(Integer::compareTo).orElse(Integer.MIN_VALUE);
        int huonoinGp = kaikkiSarjat.stream().min(Integer::compareTo).orElse(Integer.MAX_VALUE);
        long parasCount = kaikkiSarjat.stream().filter(s -> s == parasGp).count();
        long huonoinCount = kaikkiSarjat.stream().filter(s -> s == huonoinGp).count();

        for (Tulos tulos : osallistuneet) {
            Keilaaja k = tulos.getKeilaaja();
            int s1 = Optional.ofNullable(tulos.getSarja1()).orElse(0);
            int s2 = Optional.ofNullable(tulos.getSarja2()).orElse(0);
            int omaParas = Math.max(s1, s2);
            int omaHuonoin = Math.min(s1, s2);

            // Kategoria 1: henkilökohtainen (vain jos kaudelta on jo aiempia tuloksia)
            keilaajaKausiRepository.findByKeilaajaAndKausi(k, gp.getKausi()).ifPresent(kk -> {
                Integer kaudenParas = kk.getParasSarja();
                Integer kaudenHuonoin = kk.getHuonoinSarja();
                if (kaudenParas != null && omaParas >= kaudenParas) {
                    merge(k.getKeilaajaId(), +1);
                }
                if (kaudenHuonoin != null && omaHuonoin <= kaudenHuonoin) {
                    merge(k.getKeilaajaId(), -1);
                }
            });

            // Kategoria 2: GP:n paras/huonoin (tasatilanne jaetaan kaikkien kesken)
            if (omaParas == parasGp && parasCount > 0) {
                merge(k.getKeilaajaId(), 1.0 / parasCount);
            }
            if (omaHuonoin == huonoinGp && huonoinCount > 0) {
                merge(k.getKeilaajaId(), -1.0 / huonoinCount);
            }
        }

        // Rajaa yhdestä GP:stä kertyvät pisteet [-2, +2]
        keilaajaPisteetMap.replaceAll((id, p) -> Math.max(-2.0, Math.min(2.0, p)));

        // Tallenna idempotentisti vain osallistujille
        tallennaPisteet(gp, osallistuneet);

        keilaajaPisteetMap.clear();
    }

    private void merge(Long keilaajaId, double delta) {
        keilaajaPisteetMap.merge(keilaajaId, delta, Double::sum);
    }

    private void tallennaPisteet(GP gp, List<Tulos> osallistuneet) {
        // Poista vanhat rivit tältä GP:ltä → ei duplikaatteja vaikka metodi ajettaisiin kahdesti
        kultainenGpRepository.deleteByGp(gp);

        for (Tulos t : osallistuneet) {
            Keilaaja k = t.getKeilaaja();
            double pisteet = keilaajaPisteetMap.getOrDefault(k.getKeilaajaId(), 0.0);

            KultainenGp rivi = new KultainenGp();
            rivi.setGp(gp);
            rivi.setKeilaaja(k);
            rivi.setLisapisteet(pisteet);
            kultainenGpRepository.save(rivi);

            // Päivitä myös kausipisteet (vain osallistuneille)
            keilaajaKausiRepository.findByKeilaajaAndKausi(k, gp.getKausi()).ifPresent(kk -> {
                kk.setKaudenPisteet(kk.getKaudenPisteet() + pisteet);
                keilaajaKausiRepository.save(kk);
            });
        }
    }
}
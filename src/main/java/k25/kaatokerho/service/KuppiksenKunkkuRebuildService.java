package k25.kaatokerho.service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import k25.kaatokerho.domain.GP;
import k25.kaatokerho.domain.GpRepository;
import k25.kaatokerho.domain.Kausi;
import k25.kaatokerho.domain.KausiRepository;
import k25.kaatokerho.domain.KuppiksenKunkku;
import k25.kaatokerho.domain.KuppiksenKunkkuRepository;
import k25.kaatokerho.exception.ApiException;

@Service
public class KuppiksenKunkkuRebuildService {

    private final GpRepository gpRepository;
    private final KuppiksenKunkkuRepository kkRepository;
    private final KuppiksenKunkkuService kuppisService;

    public KuppiksenKunkkuRebuildService(GpRepository gpRepository,
            KuppiksenKunkkuRepository kkRepository,
            KuppiksenKunkkuService kuppisService) {
        this.gpRepository = gpRepository;
        this.kkRepository = kkRepository;
        this.kuppisService = kuppisService;
    }

    /**
     * Rebuildaa sen kauden KuppiksenKunkku-ketjun, johon annettu GP kuuluu.
     */
    @Transactional
    public void rebuildForGp(Long gpId) {
        GP gp = gpRepository.findById(gpId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "GP:tä ei löytynyt ID:llä " + gpId));

        Kausi kausi = gp.getKausi();
        if (kausi == null || kausi.getKausiId() == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "GP:llä ei ole kausitietoa.");
        }

        rebuildSeason(kausi.getKausiId());
    }

    /**
     * Rebuildaa yksittäisen kauden koko KK-ketjun.
     * Käytetään sekä tulosten poistossa että GP:n poiston jälkeen.
     */
    @Transactional
    public void rebuildSeason(Long kausiId) {

        // 1) Hae kauden nykyiset KK-rivit ja ota talteen vyoUnohtui per GP
        List<KuppiksenKunkku> vanhat = kkRepository.findByGp_Kausi_KausiId(kausiId);

        Map<Long, Boolean> vyoByGpId = vanhat.stream()
                .filter(kk -> kk.getGp() != null && kk.getGp().getGpId() != null)
                .collect(Collectors.toMap(
                        kk -> kk.getGp().getGpId(),
                        kk -> Boolean.TRUE.equals(kk.isVyoUnohtui()),
                        (a, b) -> a // yhdistää mahdolliset duplikaatit
                ));

        // 2) Poista vain tämän kauden KK-rivit
        kkRepository.deleteAll(vanhat);

        // 3) Hae kauden GP:t järjestyksessä
        List<GP> gpList = gpRepository.findByKausi_KausiIdOrderByJarjestysnumeroAsc(kausiId);
        if (gpList.isEmpty()) {
            return;
        }

        KuppiksenKunkku edellinen = null;

        for (GP gp : gpList) {
            Long gpId = gp.getGpId();
            if (gpId == null) {
                continue;
            }

            boolean vyoUnohtui = Boolean.TRUE.equals(vyoByGpId.get(gpId));

            // Luo tai jätä luomatta KK-rivi tämän GP:n tulosten perusteella
            kuppisService.kasitteleKuppiksenKunkku(gp, edellinen, vyoUnohtui);

            // Päivitä "edellinen" vain jos tälle GP:lle syntyi KK-merkintä
            edellinen = kkRepository.findByGp_GpId(gpId).orElse(edellinen);
        }
    }
}
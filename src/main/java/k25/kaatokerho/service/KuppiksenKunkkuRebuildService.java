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
     * Rebuildaa yksittäisen kauden koko KK-ketjun.
     * Käytetään sekä tulosten poistossa että GP:n poiston jälkeen.
     */
    @Transactional
    public void rebuildSeason(Long kausiId) {

        // 1) Lue talteen vyoUnohtui per gpId ENNEN kuin poistat rivit
        List<KuppiksenKunkku> vanhat = kkRepository.findBySeasonId(kausiId);

        Map<Long, Boolean> vyoByGpId = vanhat.stream()
                .filter(kk -> kk.getGp() != null && kk.getGp().getGpId() != null)
                .collect(Collectors.toMap(
                        kk -> kk.getGp().getGpId(),
                        kk -> Boolean.TRUE.equals(kk.isVyoUnohtui()),
                        (a, b) -> a));

        // 2) Poista kauden KK-rivit varmasti yhdellä JPQL-deletellä
        int deleted = kkRepository.deleteBySeasonId(kausiId);
        kkRepository.flush();
        System.out.println("Poistettiin " + deleted + " KK-riviä kaudelta " + kausiId);

        // 3) Hae kauden GP:t järjestyksessä
        List<GP> gpList = gpRepository.findByKausi_KausiIdOrderByJarjestysnumeroAsc(kausiId);
        if (gpList.isEmpty()) {
            return;
        }

        KuppiksenKunkku edellinen = null;

        for (GP gp : gpList) {
            Long gpId = gp.getGpId();
            if (gpId == null)
                continue;

            boolean vyoUnohtui = Boolean.TRUE.equals(vyoByGpId.get(gpId));

            kuppisService.kasitteleKuppiksenKunkku(gp, edellinen, vyoUnohtui);

            edellinen = kkRepository.findByGp_GpId(gpId).orElse(edellinen);
        }
    }
}
package k25.kaatokerho.service.api;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import k25.kaatokerho.domain.*;
import k25.kaatokerho.domain.dto.KuppiksenKunkkuStatsDTO;
import k25.kaatokerho.domain.dto.KuppiksenKunkkuDTO;

@Service
@Transactional(readOnly = true)
public class KuppiksenKunkkuApiService {

    private final KuppiksenKunkkuRepository kkRepo;
    private final GpRepository gpRepo;

    public KuppiksenKunkkuApiService(KuppiksenKunkkuRepository kkRepo, GpRepository gpRepo) {
        this.kkRepo = kkRepo;
        this.gpRepo = gpRepo;
    }

    public List<KuppiksenKunkkuDTO> getSeasonHistory(String seasonName) {
        List<KuppiksenKunkku> list = kkRepo
                .findByGp_Kausi_NimiOrderByGp_JarjestysnumeroAsc(seasonName);
        return list.stream().map(this::toDto).toList();
    }

    public KuppiksenKunkkuDTO getCurrentChampion(String seasonName) {
        KuppiksenKunkku kk = kkRepo
                .findTopByGp_Kausi_NimiOrderByGp_JarjestysnumeroDesc(seasonName)
                .orElseThrow();
        return toDto(kk);
    }

    public KuppiksenKunkkuDTO getByGp(Long gpId) {
        KuppiksenKunkku kk = kkRepo.findByGp_GpId(gpId).orElseThrow();
        return toDto(kk);
    }

    public List<KuppiksenKunkkuDTO> getByPlayer(Long keilaajaId, String seasonName) {
        List<KuppiksenKunkku> list = (seasonName == null)
                ? kkRepo.findByAnyPlayer(keilaajaId)
                : kkRepo.findByAnyPlayerAndSeason(keilaajaId, seasonName);
        return list.stream().map(this::toDto).toList();
    }

    public KuppiksenKunkkuStatsDTO getSeasonStats(String seasonName) {
        var history = getSeasonHistory(seasonName);
        int gpCount = history.size();
        var current = history.isEmpty() ? null : history.getLast();
        long uniqueChampions = history.stream().map(KuppiksenKunkkuDTO::voittajaId).distinct().count();
        return new KuppiksenKunkkuStatsDTO(
                seasonName,
                gpCount,
                current != null ? current.voittajaId() : null,
                current != null ? current.voittajaNimi() : null,
                (int) uniqueChampions,
                gpCount // haaste per GP
        );
    }

    private KuppiksenKunkkuDTO toDto(KuppiksenKunkku k) {
        var gp = k.getGp();
        var puol = k.getPuolustaja();
        var haast = k.getHaastaja();
        var voit = k.getVoittaja();
        return new KuppiksenKunkkuDTO(
                k.getKuppiksenKunkkuId(),
                gp != null ? gp.getGpId() : null,
                gp != null ? gp.getJarjestysnumero() : null,
                gp != null ? gp.getPvm() : null,
                (puol != null ? puol.getKeilaajaId() : null),
                (puol != null ? puol.getEtunimi() + " " + puol.getSukunimi() : null),
                (haast != null ? haast.getKeilaajaId() : null),
                (haast != null ? haast.getEtunimi() + " " + haast.getSukunimi() : null),
                (voit != null ? voit.getKeilaajaId() : null),
                (voit != null ? voit.getEtunimi() + " " + voit.getSukunimi() : null),
                Boolean.TRUE.equals(k.isVyoUnohtui()));
    }
}
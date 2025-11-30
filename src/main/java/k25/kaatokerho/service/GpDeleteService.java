package k25.kaatokerho.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import k25.kaatokerho.domain.GP;
import k25.kaatokerho.domain.GpRepository;
import k25.kaatokerho.domain.TulosRepository;
import k25.kaatokerho.exception.ApiException;
import k25.kaatokerho.service.api.KultainenGpApiService;
import k25.kaatokerho.domain.KausiRepository;
import k25.kaatokerho.service.KuppiksenKunkkuRebuildService;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GpDeleteService {

    private final GpRepository gpRepository;
    private final KuppiksenKunkkuRebuildService kuppisRebuildService;
    private final KeilaajaKausiService kausiService;
    private final KausiRepository kausiRepository;

    public GpDeleteService(GpRepository gpRepository,
            KuppiksenKunkkuRebuildService kuppisRebuildService,
            KeilaajaKausiService kausiService,
            KausiRepository kausiRepository) {
        this.gpRepository = gpRepository;
        this.kuppisRebuildService = kuppisRebuildService;
        this.kausiService = kausiService;
        this.kausiRepository = kausiRepository;
    }

    @Transactional
    public void deleteGpCompletely(Long gpId) {
        GP gp = gpRepository.findById(gpId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "GP:tä ei löytynyt ID:llä " + gpId));

        // Otetaan talteen kausiId ENNEN kuin poistetaan GP
        var kausi = gp.getKausi();
        Long kausiId = kausi != null ? kausi.getKausiId() : null;

        // 1) Poista GP (cascade poistaa Tulos, KultainenGp, KuppiksenKunkku)
        gpRepository.delete(gp);

        // 2) Päivitä kauden gpMaara
        if (kausi != null && kausi.getGpMaara() != null && kausi.getGpMaara() > 0) {
            kausi.setGpMaara(kausi.getGpMaara() - 1);
            kausiRepository.save(kausi);
        }

        // 3) Rakenna Kuppiksen Kunkku -ketju uudestaan TÄLLE kaudelle
        if (kausiId != null) {
            kuppisRebuildService.rebuildSeason(kausiId);
        }

        // 4) Rakenna sarjataulukko uudestaan
        kausiService.paivitaKaikkiKeilaajaKausiTiedot();
    }
}
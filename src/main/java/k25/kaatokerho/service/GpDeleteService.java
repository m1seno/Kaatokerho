package k25.kaatokerho.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import k25.kaatokerho.domain.GP;
import k25.kaatokerho.domain.GpRepository;
import k25.kaatokerho.domain.TulosRepository;
import k25.kaatokerho.exception.ApiException;
import k25.kaatokerho.service.api.KultainenGpApiService;
import k25.kaatokerho.domain.KausiRepository;
import k25.kaatokerho.service.KuppiksenKunkkuRebuildService;

@Service
public class GpDeleteService {

    private final GpRepository gpRepository;
    private final TulosRepository tulosRepository;
    private final KuppiksenKunkkuRebuildService kuppisRebuildService;
    private final KultainenGpApiService kultainenService;
    private final KeilaajaKausiService kausiService;
    private final KausiRepository kausiRepository;

    public GpDeleteService(GpRepository gpRepository,
                             TulosRepository tulosRepository,
                             KuppiksenKunkkuRebuildService kuppisRebuildService,
                             KultainenGpApiService kultainenService,
                             KeilaajaKausiService kausiService,
                             KausiRepository kausiRepository) {
        this.gpRepository = gpRepository;
        this.tulosRepository = tulosRepository;
        this.kuppisRebuildService = kuppisRebuildService;
        this.kultainenService = kultainenService;
        this.kausiService = kausiService;
        this.kausiRepository = kausiRepository;
    }

    @Transactional
    public void deleteGpCompletely(Long gpId) {
        GP gp = gpRepository.findById(gpId)
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "GP:tä ei löytynyt ID:llä " + gpId));

        // 1) Riippuvuuksien siivous
        tulosRepository.deleteByGp_GpId(gpId);
        kuppisRebuildService.rebuildForGp(gpId);
        kultainenService.deleteKultainenGpIfExists(gpId); // idempotentti: ei löydy -> heittää 404 tai no-op, tee mieluusti no-op

        // 2) Poista varsinainen GP
        gpRepository.delete(gp);

        // 2b) Päivitä kauden gpMaara
        var kausi = gp.getKausi();
        if (kausi != null && kausi.getGpMaara() != null && kausi.getGpMaara() > 0) {
            kausi.setGpMaara(kausi.getGpMaara() - 1);
            kausiRepository.save(kausi);
        }

        // 3) Rakenna sarjataulukko uudestaan
        kausiService.paivitaKaikkiKeilaajaKausiTiedot();
    }
}
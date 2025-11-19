package k25.kaatokerho.service.api;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import k25.kaatokerho.domain.GP;
import k25.kaatokerho.domain.GpRepository;
import k25.kaatokerho.domain.Kausi;
import k25.kaatokerho.domain.KausiRepository;
import k25.kaatokerho.domain.Keilaaja;
import k25.kaatokerho.domain.KeilaajaRepository;
import k25.kaatokerho.domain.KultainenGp;
import k25.kaatokerho.domain.KultainenGpRepository;
import k25.kaatokerho.domain.dto.ResponseKultainenGpDTO;
import k25.kaatokerho.exception.ApiException;

@Service
public class KultainenGpApiService {

    private final KultainenGpRepository kultainenRepo;
    private final KeilaajaRepository keilaajaRepo;
    private final GpRepository gpRepo;
    private final KausiRepository kausiRepo;

    public KultainenGpApiService(KultainenGpRepository kultainenRepo, KeilaajaRepository keilaajaRepo,
            GpRepository gpRepo, KausiRepository kausiRepo) {
        this.kultainenRepo = kultainenRepo;
        this.keilaajaRepo = keilaajaRepo;
        this.gpRepo = gpRepo;
        this.kausiRepo = kausiRepo;
    }

    private ResponseKultainenGpDTO mapToDto(KultainenGp kultainenGp) {
        GP gp = kultainenGp.getGp();
        Kausi kausi = gp.getKausi();
    
        return ResponseKultainenGpDTO.builder()
                .kultainenGpId(kultainenGp.getKultainenGpId())
                .keilaajaId(kultainenGp.getKeilaaja().getKeilaajaId())
                .keilaajaNimi(
                    kultainenGp.getKeilaaja().getEtunimi() + " " +
                    kultainenGp.getKeilaaja().getSukunimi()
                )
                .gpId(gp.getGpId())
                .jarjestysnumero(gp.getJarjestysnumero())
                .kausiId(kausi.getKausiId())
                .kausiNimi(kausi.getNimi())
                .lisapisteet(kultainenGp.getLisapisteet())
                .build();
    }

    // Hakee kaikki KultainenGp-instanssit
    public List<ResponseKultainenGpDTO> getAllKGP() {
        List<KultainenGp> kultainenLista = new ArrayList<>();
        kultainenRepo.findAll().forEach(kultainenLista::add);

        return kultainenLista.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    // Hakee GP kohtaiset KGP-instanssit
    public List<ResponseKultainenGpDTO> getGPkohtaiset(Long gpId) {
        GP gp = gpRepo.findById(gpId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Gp:tä ei löytynyt id:llä " + gpId));

        List<KultainenGp> kgpLista = kultainenRepo.findByGp_GpId(gpId);
        if (kgpLista.isEmpty()) {
            throw new ApiException(HttpStatus.NOT_FOUND, "Tilastoja ei löydy GP:n id:llä " + gpId);
        }

        return kgpLista.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    // Hakee kausikohtaiset KGP-instanssit
    public List<ResponseKultainenGpDTO> getKaudenKGP(Long kausiId) {
        Kausi kausi = kausiRepo.findById(kausiId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Kautta ei löytynyt Id:llä " + kausiId));

        List<KultainenGp> kgpLista = kultainenRepo.findByGp_Kausi_KausiId(kausiId);

        if (kgpLista.isEmpty()) {
            throw new ApiException(HttpStatus.NOT_FOUND, "Tilastoja ei löydy kaudelta " + kausi.getNimi());
        }

        return kgpLista.stream()
                .map(this::mapToDto)
                .toList();

    }

    // Hakee keilaajakohtaiset KGP-instanssit
    public List<ResponseKultainenGpDTO> getKeilaajanKGP(Long keilaajaId) {
        Keilaaja keilaaja = keilaajaRepo.findById(keilaajaId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Keilaajaa ei löytynyt Id:llä " + keilaajaId));

        List<KultainenGp> kgpLista = kultainenRepo.findByKeilaaja_KeilaajaId(keilaajaId);

        if (kgpLista.isEmpty()) {
            throw new ApiException(HttpStatus.NOT_FOUND,
                    "Tilastoja ei löydy henkilöltä " + keilaaja.getEtunimi() + " " + keilaaja.getSukunimi());
        }

        return kgpLista.stream()
                .map(this::mapToDto)
                .toList();

    }

    // Hakee tietyn keilaajan kausitilastot tietyltä kaudelta
    public List<ResponseKultainenGpDTO> getKeilaajanKausiKGP(Long keilaajaId, Long kausiId) {
        Keilaaja keilaaja = keilaajaRepo.findById(keilaajaId)
                .orElseThrow(
                        () -> new ApiException(HttpStatus.NOT_FOUND, "Keilaajaa ei löytynyt ID:llä " + keilaajaId));

        Kausi kausi = kausiRepo.findById(kausiId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Kautta ei löytynyt ID:llä " + kausiId));

        List<KultainenGp> kgpLista = kultainenRepo.findByKeilaaja_KeilaajaIdAndGp_Kausi_KausiId(keilaajaId, kausiId);

        if (kgpLista.isEmpty()) {
            throw new ApiException(HttpStatus.NOT_FOUND,
                    "Kaudelta " + kausi.getNimi() + " ei löydy tilastoja henkilöltä " + keilaaja.getEtunimi() + " "
                            + keilaaja.getSukunimi());
        }

        return kgpLista.stream()
                .map(this::mapToDto)
                .toList();
    }

    // Poista KultainenGP-instanssi
    @Transactional
    public void deleteKultainenGpIfExists(Long gpId) {
        List<KultainenGp> kultainenGps = kultainenRepo.findByGp_GpId(gpId);
        if (!kultainenGps.isEmpty()) {
            kultainenGps.forEach(kultainenRepo::delete);
        }
    }
}
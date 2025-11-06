package k25.kaatokerho.service.api;

import java.util.List;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import k25.kaatokerho.domain.GP;
import k25.kaatokerho.domain.GpRepository;
import k25.kaatokerho.domain.Keilaaja;
import k25.kaatokerho.domain.KeilaajaRepository;
import k25.kaatokerho.domain.KuppiksenKunkku;
import k25.kaatokerho.domain.Tulos;
import k25.kaatokerho.domain.TulosRepository;
import k25.kaatokerho.domain.dto.LisaaTuloksetDTO;
import k25.kaatokerho.domain.dto.TulosResponseDTO;
import k25.kaatokerho.exception.ApiException;
import k25.kaatokerho.service.KeilaajaKausiService;
import k25.kaatokerho.service.KuppiksenKunkkuService;
import k25.kaatokerho.domain.KuppiksenKunkkuRepository;

@Service
public class TulosApiService {

    private final TulosRepository tulosRepository;
    private final KeilaajaRepository keilaajaRepository;
    private final GpRepository gpRepository;
    private final KeilaajaKausiService keilaajaKausiService;
    private final KuppiksenKunkkuService kuppiksenKunkkuService;
    private final KuppiksenKunkkuRepository kuppiksenKunkkuRepository;

    public TulosApiService(TulosRepository tulosRepository,
            KeilaajaRepository keilaajaRepository,
            GpRepository gpRepository,
            KeilaajaKausiService keilaajaKausiService,
            KuppiksenKunkkuService kuppiksenKunkkuService,
            KuppiksenKunkkuRepository kuppiksenKunkkuRepository) {
        this.tulosRepository = tulosRepository;
        this.keilaajaRepository = keilaajaRepository;
        this.gpRepository = gpRepository;
        this.keilaajaKausiService = keilaajaKausiService;
        this.kuppiksenKunkkuService = kuppiksenKunkkuService;
        this.kuppiksenKunkkuRepository = kuppiksenKunkkuRepository;
    }

    @Transactional
    public List<TulosResponseDTO> LisaaTaiKorvaaGpTulokset(LisaaTuloksetDTO dto) {
        GP gp = gpRepository.findById(dto.getGpId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "GP:tä ei löytynyt ID:llä " + dto.getGpId()));

        // Idempotentti: pudotetaan ensin vanhat tämän GP:n tulokset, ettei synny
        // duplikaatteja
        tulosRepository.deleteByGp_GpId(gp.getGpId());

        // Tallenna uudet
        for (LisaaTuloksetDTO.TulosForm tf : dto.getTulokset()) {
            Keilaaja keilaaja = keilaajaRepository.findById(tf.getKeilaajaId())
                    .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND,
                            "Keilaajaa ei löytynyt ID:llä " + tf.getKeilaajaId()));

            Tulos tulos = new Tulos();
            tulos.setGp(gp);
            tulos.setKeilaaja(keilaaja);
            tulos.setSarja1(tf.getSarja1());
            tulos.setSarja2(tf.getSarja2());
            tulos.setOsallistui(tf.getSarja1() != null && tf.getSarja2() != null);

            tulosRepository.save(tulos);
        }

        // Hae edellinen KK-merkintä
        var prevOpt = kuppiksenKunkkuRepository
                .findTopByGp_KausiAndGp_JarjestysnumeroLessThanOrderByGp_JarjestysnumeroDesc(gp.getKausi(), gp.getJarjestysnumero());

        // Vyötieto
        boolean vyoUnohtui = Boolean.TRUE.equals(dto.getVyoUnohtui());
        // Päivitä kuppiksen kunkku
        kuppiksenKunkkuService.kasitteleKuppiksenKunkku(gp, prevOpt.orElse(null), vyoUnohtui);

        // Päivitä kausitilastot tämän GP:n perusteella (tämä käynnistää pistelaskut)
        keilaajaKausiService.paivitaKeilaajaKausi(gp);

        // Palauta tallennetut
        return haeTuloksetGp(gp.getGpId());
    }

    @Transactional(readOnly = true)
    public List<TulosResponseDTO> haeTuloksetGp(Long gpId) {
        GP gp = gpRepository.findById(gpId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "GP:tä ei löytynyt ID:llä " + gpId));

        List<Tulos> list = tulosRepository.findByGp_GpId(gp.getGpId());
        if (list.isEmpty()) {
            throw new ApiException(HttpStatus.NOT_FOUND, "Tuloksia ei löytynyt GP:lle " + gp.getGpId());
        }
        return list.stream().map(this::mapToDto).toList();
    }

    @Transactional
    public void poistaTuloksetGp(Long gpId) {
        GP gp = gpRepository.findById(gpId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "GP:tä ei löytynyt ID:llä " + gpId));

        tulosRepository.deleteByGp_GpId(gp.getGpId());
        // HUOM: jos poistat tulokset, kausitilasto pitää laskea uudelleen kaikista
        // aiemmista GP:stä
        keilaajaKausiService.paivitaKaikkiKeilaajaKausiTiedot();
    }

    @Transactional(readOnly = true)
    public List<TulosResponseDTO> haeKeilaajanTulokset(Long keilaajaId) {
        keilaajaRepository.findById(keilaajaId)
                .orElseThrow(
                        () -> new ApiException(HttpStatus.NOT_FOUND, "Keilaajaa ei löytynyt ID:llä " + keilaajaId));

        List<Tulos> list = tulosRepository.findByKeilaaja_KeilaajaId(keilaajaId);
        if (list.isEmpty()) {
            throw new ApiException(HttpStatus.NOT_FOUND, "Tuloksia ei löytynyt keilaajalle ID: " + keilaajaId);
        }
        return list.stream().map(this::mapToDto).toList();
    }

    @Transactional(readOnly = true)
    public List<TulosResponseDTO> haeKeilaajanTuloksetKaudella(Long keilaajaId, Long kausiId) {
        keilaajaRepository.findById(keilaajaId)
                .orElseThrow(
                        () -> new ApiException(HttpStatus.NOT_FOUND, "Keilaajaa ei löytynyt ID:llä " + keilaajaId));

        List<Tulos> list = tulosRepository.findByKeilaaja_KeilaajaIdAndGp_Kausi_KausiId(keilaajaId, kausiId);
        if (list.isEmpty()) {
            throw new ApiException(HttpStatus.NOT_FOUND, "Tuloksia ei löytynyt keilaajalle/kaudelle (keilaajaId: "
                    + keilaajaId + ", kausiId: " + kausiId + ")");
        }
        return list.stream().map(this::mapToDto).toList();
    }

    private TulosResponseDTO mapToDto(Tulos t) {
        return TulosResponseDTO.builder()
                .tulosId(t.getTulosId())
                .gpId(t.getGp().getGpId())
                .keilaajaId(t.getKeilaaja().getKeilaajaId())
                .keilaajaEtunimi(t.getKeilaaja().getEtunimi())
                .keilaajaSukunimi(t.getKeilaaja().getSukunimi())
                .sarja1(t.getSarja1())
                .sarja2(t.getSarja2())
                .osallistui(Boolean.TRUE.equals(t.getOsallistui()))
                .build();
    }
}
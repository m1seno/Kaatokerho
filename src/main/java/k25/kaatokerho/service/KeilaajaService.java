package k25.kaatokerho.service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import k25.kaatokerho.domain.Kausi;
import k25.kaatokerho.domain.Keilaaja;
import k25.kaatokerho.domain.KeilaajaRepository;
import k25.kaatokerho.domain.dto.KausiResponseDTO;
import k25.kaatokerho.domain.dto.KeilaajaResponseDTO;
import k25.kaatokerho.domain.dto.UusiKausiDTO;
import k25.kaatokerho.domain.dto.UusiKeilaajaDTO;
import k25.kaatokerho.exception.ApiException;

@Service
public class KeilaajaService {

    private final KeilaajaRepository keilaajaRepository;

    public KeilaajaService(KeilaajaRepository keilaajaRepository) {
        this.keilaajaRepository = keilaajaRepository;
    }

    private KeilaajaResponseDTO mapToDto(Keilaaja keilaaja) {
        return KeilaajaResponseDTO.builder()
                .etunimi(keilaaja.getEtunimi())
                .sukunimi(keilaaja.getSukunimi())
                .syntymapaiva(keilaaja.getSyntymapaiva())
                .aktiivijasen(keilaaja.getAktiivijasen())
                .admin(keilaaja.getAdmin())
                .kayttajanimi(keilaaja.getKayttajanimi())
                .build();
    }

    // Haetaan yhden keilaajan tiedot
    public KeilaajaResponseDTO getKeilaajaById(Long keilaajaId) {
        Keilaaja keilaaja = keilaajaRepository.findById(keilaajaId).orElse(null);
        if (keilaaja == null) {
            throw new ApiException(HttpStatus.NOT_FOUND, "Keilaajaa ei löydy id:llä " + keilaajaId);
        }

        return mapToDto(keilaaja);
    }

    // Haetaan lista kaikista keilaajista
    public List<KeilaajaResponseDTO> getAllKeilaajat() {

        Iterable<Keilaaja> iterableKelaaja = keilaajaRepository.findAll();

        List<Keilaaja> keilaajaLista = new ArrayList<>();

        for (Keilaaja keilaaja : iterableKelaaja) {
            keilaajaLista.add(keilaaja);
        }

        return keilaajaLista.stream().map(keilaaja -> {
            KeilaajaResponseDTO responseDto = getKeilaajaById(keilaaja.getKeilaajaId()); // Muokkaa keilaaja
                                                                                         // entityt
                                                                                         // KeilaajaResponseDTO
                                                                                         // -objekteiksi
            return responseDto;
        }).collect(Collectors.toList());
    }

    // Lisää uusi keilaaja
    public KeilaajaResponseDTO addNewKeilaaja(UusiKeilaajaDTO dto) {

        String kayttajanimi = dto.getKayttajanimi().trim();

        if (kayttajanimi != null && keilaajaRepository.findByKayttajanimi(kayttajanimi).equals(kayttajanimi)) {
            throw new ApiException(HttpStatus.BAD_REQUEST,
                    "Käyttäjänimi " + kayttajanimi + " on jo käytössä toisella käyttäjällä");
        } else {
            Keilaaja keilaaja = new Keilaaja();
            keilaaja.setEtunimi(dto.getEtunimi());
            keilaaja.setSukunimi(dto.getSukunimi());
            keilaaja.setSyntymapaiva(dto.getSyntymapaiva());
            keilaaja.setAktiivijasen(dto.getAktiivijasen());
            keilaaja.setAdmin(dto.getAdmin());

            if (dto.getAdmin()) {
                keilaaja.setKayttajanimi(dto.getKayttajanimi());
                keilaaja.setSalasanaHash(dto.getSalasana());
            } else {
                keilaaja.setKayttajanimi(null);
                keilaaja.setSalasanaHash(null);
            }
            return mapToDto(keilaajaRepository.save(keilaaja));

        }
    }

    //Päivitä keilaaja
    public KeilaajaResponseDTO updateKeilaaja(Long keilaajaId, UusiKeilaajaDTO dto) {
        Keilaaja keilaaja = keilaajaRepository.findById(keilaajaId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Keilaajaa ei löytynyt ID:llä " + keilaajaId));

        String kayttajanimi = dto.getKayttajanimi().trim();

        if (kayttajanimi != null && !kayttajanimi.equals(keilaaja.getKayttajanimi()) &&
        keilaajaRepository.findByKayttajanimi(kayttajanimi).equals(kayttajanimi)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Käyttäjänimi " + kayttajanimi + " on jo olemassa toisella keilaajalla.");
        }

        keilaaja.setEtunimi(dto.getEtunimi());
        keilaaja.setSukunimi(dto.getSukunimi());
        keilaaja.setSyntymapaiva(dto.getSyntymapaiva());
            keilaaja.setAktiivijasen(dto.getAktiivijasen());
            keilaaja.setAdmin(dto.getAdmin());

            if (dto.getAdmin()) {
                keilaaja.setKayttajanimi(dto.getKayttajanimi());
                keilaaja.setSalasanaHash(dto.getSalasana());
            } else {
                keilaaja.setKayttajanimi(null);
                keilaaja.setSalasanaHash(null);
            }
            return mapToDto(keilaajaRepository.save(keilaaja));
    }

    // Poista keilaaja
    public void deleteKeilaaja(Long keilaajaId) {
        Keilaaja keilaaja = keilaajaRepository.findById(keilaajaId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Keilaajaa ei löytynyt ID:llä " + keilaajaId));

        keilaajaRepository.delete(keilaaja);
    }
}

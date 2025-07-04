package k25.kaatokerho.service.api;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import k25.kaatokerho.domain.Keilaaja;
import k25.kaatokerho.domain.KeilaajaRepository;
import k25.kaatokerho.domain.dto.KeilaajaResponseDTO;
import k25.kaatokerho.domain.dto.PaivitaSalasanaDTO;
import k25.kaatokerho.domain.dto.UusiKeilaajaDTO;
import k25.kaatokerho.exception.ApiException;

@Service
public class KeilaajaApiService {

    private final KeilaajaRepository keilaajaRepository;
    private final PasswordEncoder passwordEncoder;

    public KeilaajaApiService(KeilaajaRepository keilaajaRepository, PasswordEncoder passwordEncoder) {
        this.keilaajaRepository = keilaajaRepository;
        this.passwordEncoder = passwordEncoder;
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
        Keilaaja keilaaja = keilaajaRepository.findById(keilaajaId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Keilaajaa ei löydy id:llä " + keilaajaId));

        return mapToDto(keilaaja);
    }

    // Haetaan lista kaikista keilaajista
    public List<KeilaajaResponseDTO> getAllKeilaajat() {
        List<Keilaaja> keilaajaLista = new ArrayList<>();
        keilaajaRepository.findAll().forEach(keilaajaLista::add);

        return keilaajaLista.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    // Lisää uusi keilaaja
    public KeilaajaResponseDTO addNewKeilaaja(UusiKeilaajaDTO dto) {
        String kayttajanimi = dto.getKayttajanimi() != null ? dto.getKayttajanimi().trim() : null;

        if (kayttajanimi != null && keilaajaRepository.findByKayttajanimi(kayttajanimi).isPresent()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Käyttäjänimi " + kayttajanimi + " on jo käytössä.");
        }

        Keilaaja keilaaja = new Keilaaja();
        keilaaja.setEtunimi(dto.getEtunimi());
        keilaaja.setSukunimi(dto.getSukunimi());
        keilaaja.setSyntymapaiva(dto.getSyntymapaiva());
        keilaaja.setAktiivijasen(dto.getAktiivijasen());
        keilaaja.setAdmin(dto.getAdmin());

        if (dto.getAdmin()) {
            keilaaja.setKayttajanimi(kayttajanimi);
            keilaaja.setSalasanaHash(passwordEncoder.encode(dto.getSalasana()));
        } else {
            keilaaja.setKayttajanimi(null);
            keilaaja.setSalasanaHash(null);
        }

        return mapToDto(keilaajaRepository.save(keilaaja));
    }

    // Päivitä kaikki keilaajan tiedot paitsi salasana
    public KeilaajaResponseDTO updateKeilaaja(Long keilaajaId, UusiKeilaajaDTO dto) {
        Keilaaja keilaaja = keilaajaRepository.findById(keilaajaId)
                .orElseThrow(
                        () -> new ApiException(HttpStatus.NOT_FOUND, "Keilaajaa ei löytynyt ID:llä " + keilaajaId));

        String kayttajanimi = dto.getKayttajanimi() != null ? dto.getKayttajanimi().trim() : null;

        if (kayttajanimi != null &&
                !kayttajanimi.equals(keilaaja.getKayttajanimi()) &&
                keilaajaRepository.findByKayttajanimi(kayttajanimi).isPresent()) {

            throw new ApiException(HttpStatus.BAD_REQUEST, "Käyttäjänimi " + kayttajanimi + " on jo käytössä.");
        }

        keilaaja.setEtunimi(dto.getEtunimi());
        keilaaja.setSukunimi(dto.getSukunimi());
        keilaaja.setSyntymapaiva(dto.getSyntymapaiva());
        keilaaja.setAktiivijasen(dto.getAktiivijasen());
        keilaaja.setAdmin(dto.getAdmin());

        if (dto.getAdmin()) {
            keilaaja.setKayttajanimi(kayttajanimi);
        } else {
            keilaaja.setKayttajanimi(null);
        }
        return mapToDto(keilaajaRepository.save(keilaaja));
    }

    // Päivitä keilaajan salasana
    public void updateSalasana(Long keilaajaId, PaivitaSalasanaDTO dto) {
        Keilaaja keilaaja = keilaajaRepository.findById(keilaajaId)
                .orElseThrow(
                        () -> new ApiException(HttpStatus.NOT_FOUND, "Keilaajaa ei löytynyt ID:llä " + keilaajaId));

        if (!passwordEncoder.matches(dto.getVanhaSalasana(), keilaaja.getSalasanaHash())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Väärä vanha salasana");
        }
        keilaaja.setSalasanaHash(passwordEncoder.encode(dto.getUusiSalasana()));
        keilaajaRepository.save(keilaaja);
    }

    // Poista keilaaja
    public void deleteKeilaaja(Long keilaajaId) {
        Keilaaja keilaaja = keilaajaRepository.findById(keilaajaId)
                .orElseThrow(
                        () -> new ApiException(HttpStatus.NOT_FOUND, "Keilaajaa ei löytynyt ID:llä " + keilaajaId));

        keilaajaRepository.delete(keilaaja);
    }
}

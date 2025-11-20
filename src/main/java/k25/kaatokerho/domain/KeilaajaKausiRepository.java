package k25.kaatokerho.domain;

import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

public interface KeilaajaKausiRepository extends CrudRepository<KeilaajaKausi, Long> {

    Optional<KeilaajaKausi> findByKeilaajaAndKausi(Keilaaja keilaaja, Kausi kausi);
    List<KeilaajaKausi> findByKausi_KausiId(Long kausiId);
    List<KeilaajaKausi> findByKeilaaja_KeilaajaId(Long keilaajaId);
    List<KeilaajaKausi> findByKausi(Kausi kausi);
}

package k25.kaatokerho.domain;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

public interface KausiRepository extends CrudRepository<Kausi, Long> {

    Optional<Kausi> findByNimi(String nimi);
    Kausi findTopByOrderByKausiIdDesc();
}

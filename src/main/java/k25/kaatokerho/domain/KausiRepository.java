package k25.kaatokerho.domain;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface KausiRepository extends JpaRepository<Kausi, Long> {

    Optional<Kausi> findByNimi(String nimi);
    Kausi findTopByOrderByKausiIdDesc();
}

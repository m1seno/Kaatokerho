package k25.kaatokerho.domain;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

public interface KeilahalliRepository extends CrudRepository<Keilahalli, Long> {

    Optional<Keilahalli> findByNimiContainingIgnoreCase(String hakusana);
}

package k25.kaatokerho.domain;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

public interface KeilaajaKausiRepository extends CrudRepository<KeilaajaKausi, Long> {

    Optional<KeilaajaKausi> findByKeilaajaAndKausi(Long keilaajaId, Long kausiId);
    
}

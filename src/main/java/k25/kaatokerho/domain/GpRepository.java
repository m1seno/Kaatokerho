package k25.kaatokerho.domain;

import java.time.LocalDate;
import java.util.Optional;

import org.springframework.data.repository.CrudRepository;


public interface GpRepository extends CrudRepository<GP, Long> {
    Optional<GP> findByPvm(LocalDate pvm);
    GP findByJarjestysnumero(Integer jarjestysnumero);
}

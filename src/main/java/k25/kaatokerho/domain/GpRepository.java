package k25.kaatokerho.domain;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface GpRepository extends JpaRepository<GP, Long> {
    Optional<GP> findByPvm(LocalDate pvm);

    GP findByJarjestysnumero(Integer jarjestysnumero);

    Integer countByKausi(Kausi kausi);

    List<GP> findByKausi_NimiOrderByJarjestysnumeroAsc(String seasonName);
}

package k25.kaatokerho.domain;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface GpRepository extends JpaRepository<GP, Long> {
    Optional<GP> findByPvm(LocalDate pvm);

    GP findByJarjestysnumero(Integer jarjestysnumero);

    Integer countByKausi(Kausi kausi);

    List<GP> findByKausi_KausiIdOrderByJarjestysnumeroAsc(Long kausiId);

    Integer countByKausiAndOnKultainenGpTrue(Kausi kausi);

    List<GP> findByKausi(Kausi kausi);

    // Seuraavat GP:t kaudelta, joilla EI ole yhtään tulosta
    List<GP> findByKausi_KausiIdAndTuloksetIsEmptyOrderByJarjestysnumeroAsc(Long kausiId);
}

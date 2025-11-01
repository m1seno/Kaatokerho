package k25.kaatokerho.domain;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

public interface KuppiksenKunkkuRepository extends CrudRepository<KuppiksenKunkku, Long>{
    Optional<KuppiksenKunkku> findTopByGp_KausiAndGp_JarjestysnumeroLessThanOrderByGp_JarjestysnumeroDesc(
    Kausi kausi, int jarjestysnumero);
}

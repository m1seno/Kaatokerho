package k25.kaatokerho.domain;

import org.springframework.data.repository.CrudRepository;

public interface TulosRepository extends CrudRepository<Tulos, Long> {

    Tulos findByGpAndKeilaaja(GP gp, Keilaaja keilaaja);

}

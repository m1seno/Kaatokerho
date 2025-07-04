package k25.kaatokerho.domain;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

public interface KeilaajaRepository extends CrudRepository<Keilaaja, Long> {

    Optional<Keilaaja> findByEtunimiAndSukunimi(String etunimi, String sukunimi);
    Optional<Keilaaja> findByKayttajanimi(String kayttajanimi);
}

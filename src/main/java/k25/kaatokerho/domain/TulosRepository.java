package k25.kaatokerho.domain;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

public interface TulosRepository extends CrudRepository<Tulos, Long> {

    Tulos findByGpAndKeilaaja(GP gp, Keilaaja keilaaja);

    List<Tulos> findByGp_GpId(Long gpId);

    void deleteByGp_GpId(Long gpId);

    List<Tulos> findByKeilaaja_KeilaajaId(Long keilaajaId);

    // Valinnainen: rajaus kauden mukaan
    List<Tulos> findByKeilaaja_KeilaajaIdAndGp_Kausi_KausiId(Long keilaajaId, Long kausiId);

}

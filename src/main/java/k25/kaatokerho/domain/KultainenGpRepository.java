package k25.kaatokerho.domain;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

public interface KultainenGpRepository extends CrudRepository<KultainenGp, Long> {

    List<KultainenGp> findByGp_GpId(Long gpId);
    List<KultainenGp> findByGp_Kausi_KausiId(Long kausiId);
    List<KultainenGp> findByKeilaaja_KeilaajaId(Long keilaajaId);

    // Keilaaja + kausi yhdistelmä: Keilaaja → (KultainenGp) → GP → Kausi
    List<KultainenGp> findByKeilaaja_KeilaajaIdAndGp_Kausi_KausiId(Long keilaajaId, Long kausiId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from KultainenGp k where k.gp = :gp")
    void deleteByGp(GP gp);
    Optional<KultainenGp> findByGpAndKeilaaja(GP gp, Keilaaja keilaaja);
}

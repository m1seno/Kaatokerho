package k25.kaatokerho.domain;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

public interface KuppiksenKunkkuRepository extends CrudRepository<KuppiksenKunkku, Long> {
    Optional<KuppiksenKunkku> findByGp_Id(Long gpId);

    List<KuppiksenKunkku> findByGp_Kausi_NimiOrderByGp_JarjestysnumeroAsc(String seasonName);

    Optional<KuppiksenKunkku> findTopByGp_Kausi_NimiOrderByGp_JarjestysnumeroDesc(String seasonName);

    Optional<KuppiksenKunkku> findTopByGp_KausiAndGp_JarjestysnumeroLessThanOrderByGp_JarjestysnumeroDesc(
            Kausi kausi, int jarjestysnumero);

    long deleteByGp_GpId(Long gpId);

    // Pelaajasuodatus millä tahansa roolilla (puolustaja/haastaja/voittaja)
    @Query("""
            select k from KuppiksenKunkku k
            where k.puolustaja.id = :keilaajaId
               or k.haastaja.id   = :keilaajaId
               or k.voittaja.id   = :keilaajaId
            order by k.gp.jarjestysnumero asc
            """)
    List<KuppiksenKunkku> findByAnyPlayer(Long keilaajaId);

    @Query("""
            select k from KuppiksenKunkku k
            where k.gp.kausi.nimi = :seasonName
              and (k.puolustaja.id = :keilaajaId
                or k.haastaja.id   = :keilaajaId
                or k.voittaja.id   = :keilaajaId)
            order by k.gp.jarjestysnumero asc
            """)
    List<KuppiksenKunkku> findByAnyPlayerAndSeason(Long keilaajaId, String seasonName);
}

package k25.kaatokerho.domain;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface KuppiksenKunkkuRepository extends JpaRepository<KuppiksenKunkku, Long> {

  // Hae kauden kaikki KK-rivit
  @Query("select kk from KuppiksenKunkku kk where kk.gp.kausi.kausiId = :kausiId")
  List<KuppiksenKunkku> findBySeasonId(@Param("kausiId") Long kausiId);

  // Poista kauden kaikki KK-rivit yhdellä suoralla delete-kyselyllä
  @Modifying
  @Query("delete from KuppiksenKunkku kk where kk.gp.kausi.kausiId = :kausiId")
  int deleteBySeasonId(@Param("kausiId") Long kausiId);

  Optional<KuppiksenKunkku> findByGp_GpId(Long gpId);

  List<KuppiksenKunkku> findByGp_Kausi_NimiOrderByGp_JarjestysnumeroAsc(String seasonName);

  List<KuppiksenKunkku> findByGp_Kausi_KausiId(Long kausiId);

  Optional<KuppiksenKunkku> findTopByGp_Kausi_NimiOrderByGp_JarjestysnumeroDesc(String seasonName);

  Optional<KuppiksenKunkku> findTopByGp_KausiAndGp_JarjestysnumeroLessThanOrderByGp_JarjestysnumeroDesc(
      Kausi kausi, int jarjestysnumero);

  Optional<KuppiksenKunkku> findTopByOrderByKuppiksenKunkkuIdDesc();

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

package k25.kaatokerho.domain;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

public interface KultainenGpRepository extends CrudRepository<KultainenGp, Long> {

    List<KultainenGp> findByGp_GpId(Long gpId);
    List<KultainenGp> findByKausi_KausiId(Long kausiId);
    List<KultainenGp> findByKeilaaja_KeilaajaId(Long keilaajaId);
    List<KultainenGp> findByKeilaajanKausi(Long keilaajaId, Long kausiId);
}

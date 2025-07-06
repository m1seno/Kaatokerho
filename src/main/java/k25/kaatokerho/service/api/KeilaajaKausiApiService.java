package k25.kaatokerho.service.api;

import org.springframework.stereotype.Service;

import k25.kaatokerho.domain.KeilaajaKausiRepository;

@Service
public class KeilaajaKausiApiService {

    private final KeilaajaKausiRepository keilaajaKausiRepository;

    public KeilaajaKausiApiService(KeilaajaKausiRepository keilaajaKausiRepository){
        this.keilaajaKausiRepository = keilaajaKausiRepository;
    }

    // Hakee kaikki keilaajakausitiedot (kaikista kausista ja keilaajista)


    // Hakee kauden kaikkien keilaajien tilastot (esim. sarjataulukkoa varten)


    // Hakee yksittäisen keilaajan tilastot kaikilta kausilta


    // Hakee tietyn keilaajan kausitilastot tietyltä kaudelta

}

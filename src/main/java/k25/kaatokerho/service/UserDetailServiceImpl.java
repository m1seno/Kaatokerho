package k25.kaatokerho.service;

import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import k25.kaatokerho.domain.Keilaaja;
import k25.kaatokerho.domain.KeilaajaRepository;

@Service
public class UserDetailServiceImpl implements UserDetailsService {

    KeilaajaRepository repository;

    public UserDetailServiceImpl(KeilaajaRepository keilaajaRepository) {
        this.repository = keilaajaRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String kayttajanimi) throws UsernameNotFoundException {

        Keilaaja currentUser = repository.findByKayttajanimi(kayttajanimi);
        if (currentUser == null) {
            throw new UsernameNotFoundException("Käyttäjää ei löytynyt: " + kayttajanimi);
        }

        // Jos on admin, annetaan ROLE_ADMIN, muuten ROLE_USER
        String role = currentUser.getAdmin() ? "ROLE_ADMIN" : "ROLE_USER";

        UserDetails user = new org.springframework.security.core.userdetails.User(kayttajanimi,
                currentUser.getSalasanaHash(),
                AuthorityUtils.createAuthorityList(role));

        return user;
    }

}

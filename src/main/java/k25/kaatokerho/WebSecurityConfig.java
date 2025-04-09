package k25.kaatokerho;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import static org.springframework.security.web.util.matcher.AntPathRequestMatcher.antMatcher;

@Configuration
@EnableMethodSecurity(securedEnabled= true)
public class WebSecurityConfig {

    private UserDetailsService userDetailsService;

    public WebSecurityConfig(UserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    private static final AntPathRequestMatcher[] WHITE_LIST_URLS = {
        new AntPathRequestMatcher("/home**"),
        new AntPathRequestMatcher("/login**"),
        new AntPathRequestMatcher("/sarjataulukko**")
    };

    @Bean
    public SecurityFilterChain configure(HttpSecurity http) throws Exception {

        http.authorizeHttpRequests(
            authorize -> authorize
            .requestMatchers(antMatcher("/css/**")).permitAll()
            .requestMatchers(antMatcher("/images/**")).permitAll()
            .requestMatchers(WHITE_LIST_URLS).permitAll()
            .anyRequest().authenticated())
            .headers(headers ->
            headers.frameOptions(frameOptions -> frameOptions
            .disable()))
            .formLogin(form -> form
                .defaultSuccessUrl("/home", true)
                .permitAll())
            .logout(logout -> logout
            .logoutSuccessUrl("/home?logout")
            .permitAll())
            .csrf(csrf -> csrf.disable());
        return http.build();
    }

    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsService).passwordEncoder(new BCryptPasswordEncoder());
    }
}

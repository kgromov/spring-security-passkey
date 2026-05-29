package org.kgromov.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.ott.OneTimeToken;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authorization.EnableMultiFactorAuthentication;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.authority.FactorGrantedAuthority;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.ott.OneTimeTokenGenerationSuccessHandler;
import org.springframework.security.web.authentication.ott.RedirectOneTimeTokenGenerationSuccessHandler;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;

import static org.springframework.security.config.Customizer.withDefaults;

@EnableMultiFactorAuthentication(authorities = {
        FactorGrantedAuthority.PASSWORD_AUTHORITY,
//        FactorGrantedAuthority.OTT_AUTHORITY
        FactorGrantedAuthority.WEBAUTHN_AUTHORITY
})
@EnableWebSecurity(debug = true)
@Configuration
public class SecurityConfig {

    // The same as with Customizer - more traditional way
    @Profile("filter-chain")
    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) {
        return http
                .authorizeHttpRequests((authorize) -> authorize
                        .requestMatchers("/", "/ott/sent").permitAll()
                        .anyRequest().authenticated())
                .webAuthn(a -> a.allowedOrigins("http://localhost:8080/")
                        .rpName("kgromov")
                        .rpId("localhost")
                )
                .formLogin(withDefaults())
                .oneTimeTokenLogin(_ -> new OttSuccessHandler())
                .build();
    }

    @Bean
    Customizer<HttpSecurity> httpSecurityCustomizer() {
        return http -> http
                .webAuthn(a -> a.allowedOrigins("http://localhost:8080/")
                        .rpName("kgromov")
                        .rpId("localhost")
                        // HttpSessionPublicKeyCredentialCreationOptionsRepository
//                        .creationOptionsRepository(publicKeyCredentialRepository)
                )
                .oneTimeTokenLogin(ott ->
                        ott.tokenGenerationSuccessHandler((request, response, oneTimeToken) -> {
                            response.getWriter().println("you've got console mail!");
                            response.setContentType(MediaType.TEXT_PLAIN_VALUE);
                            IO.println("please go to http://localhost:8080/login/ott?token=" +
                                    oneTimeToken.getTokenValue());
                        })
                );
    }

    @Bean
    OneTimeTokenGenerationSuccessHandler ottSuccessHandler() {
        return new OttSuccessHandler();
    }


    // More verbose implementation of OneTimeTokenGenerationSuccessHandler with redirection
    private static class OttSuccessHandler implements OneTimeTokenGenerationSuccessHandler {
        // this is a bit redundant, but it's just for the example.
        // redirect can be removed and redirectHandler.handle not invoked - since it's required to permit "/ott/sent"
        // default redirect - `ott/generate`
        private final OneTimeTokenGenerationSuccessHandler redirectHandler = new RedirectOneTimeTokenGenerationSuccessHandler("/ott/sent");

        @Override
        public void handle(HttpServletRequest request, HttpServletResponse response, OneTimeToken oneTimeToken) throws IOException, ServletException {
            String magicLink = ServletUriComponentsBuilder.fromCurrentContextPath()
                    .path("/login/ott")
                    .queryParam("token", oneTimeToken.getTokenValue())
                    .toUriString();
            response.getWriter().println("you've got console mail!");
            response.setContentType(MediaType.TEXT_PLAIN_VALUE);
            IO.println("please go to " + magicLink);
            this.redirectHandler.handle(request, response, oneTimeToken);
        }
    }
}

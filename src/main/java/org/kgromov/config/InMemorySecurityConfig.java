package org.kgromov.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.authentication.ott.OneTimeTokenService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.provisioning.UserDetailsManager;

import java.time.Duration;
import java.util.Set;

@Profile({"inMemory", "!jdbc", "default"})
@Configuration
public class InMemorySecurityConfig {

    @Bean
    UserDetailsManager inMemoryUserDetailsManager() {
        return new InMemoryUserDetailsManager(
                Set.of(
                        User.withUsername("user")
                                .password("{noop}user")
                                .roles("USER")
                                .build(),
                        User.withDefaultPasswordEncoder()
                                .username("admin")
                                .password("admin")
                                .roles("ADMIN", "USER")
                                .build()
                )
        );
    }

    @Bean
    OneTimeTokenService pinOneTimeTokenService(@Value("${otp.duration:3m}") Duration duration) {
        return new PinOneTimeTokenService(duration);
    }
}

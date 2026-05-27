package org.kgromov.config;

import org.jspecify.annotations.NonNull;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import org.springframework.stereotype.Component;

import java.util.Set;

@Profile("jdbc")
@Component
public class UsersPopulator implements CommandLineRunner {
    private final JdbcUserDetailsManager jdbcUserDetailsManager;

    public UsersPopulator(JdbcUserDetailsManager jdbcUserDetailsManager) {
        this.jdbcUserDetailsManager = jdbcUserDetailsManager;
    }

    @Override
    public void run(String @NonNull ... args) {
        jdbcUserDetailsManager.setEnableUpdatePassword(true);
        Set<UserDetails> users = Set.of(
                User.withUsername("user")
                        .password("{noop}user")
                        .roles("USER")
                        .build(),
                User.withDefaultPasswordEncoder()
                        .username("admin")
                        .password("admin")
                        .roles("ADMIN", "USER")
                        .build()
        );
        users.forEach(jdbcUserDetailsManager::createUser);
    }
}

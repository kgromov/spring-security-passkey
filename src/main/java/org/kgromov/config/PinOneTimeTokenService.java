package org.kgromov.config;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.jspecify.annotations.Nullable;
import org.springframework.security.authentication.ott.*;

import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

class PinOneTimeTokenService implements OneTimeTokenService {
    private final Clock start;
    private final Duration ottTtl;
    private final Cache<String, OneTimeToken> tokens;

    public PinOneTimeTokenService(Duration ottTtl) {
        this.start = Clock.systemUTC();
        this.ottTtl = ottTtl;
        this.tokens = Caffeine.newBuilder()
                .expireAfterWrite(ottTtl)
                .maximumSize(100)
                .build();
    }

    @Override
    public OneTimeToken generate(GenerateOneTimeTokenRequest request) {
        String pin = this.generateCode();
        Instant expiresAt = this.start.instant().plus(this.ottTtl);
        OneTimeToken ott = new DefaultOneTimeToken(pin, request.getUsername(), expiresAt);
        this.tokens.put(pin, ott);
        return ott;
    }

    @Override
    public @Nullable OneTimeToken consume(OneTimeTokenAuthenticationToken authenticationToken) {
        String pin = authenticationToken.getTokenValue();
        return this.tokens.asMap().remove(pin);
    }

    private String generateCode() {
        SecureRandom random = new SecureRandom();
        int code = random.nextInt(999_999);
        return String.format("%06d", code);
    }
}

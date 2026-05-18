package com.urlshortener.infrastructure.adapter;

import com.urlshortener.domain.port.out.ShortCodeGeneratorPort;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;

/**
 * Generates a random 7-character Base62 string.
 * 62^7 ≈ 3.5 trillion combinations — collision probability is negligible.
 * SecureRandom is thread-safe and cryptographically strong.
 */
@Component
public class ShortCodeGeneratorAdapter implements ShortCodeGeneratorPort {

    private static final String ALPHABET   = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int    CODE_LENGTH = 7;
    private final SecureRandom  random      = new SecureRandom();

    @Override
    public String generate() {
        StringBuilder sb = new StringBuilder(CODE_LENGTH);
        for (int i = 0; i < CODE_LENGTH; i++) {
            sb.append(ALPHABET.charAt(random.nextInt(ALPHABET.length())));
        }
        return sb.toString();
    }
}

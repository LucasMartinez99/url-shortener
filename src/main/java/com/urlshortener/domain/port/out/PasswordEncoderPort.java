package com.urlshortener.domain.port.out;

/**
 * OUTPUT PORT for password hashing.
 *
 * The domain knows it needs to hash passwords but doesn't know HOW.
 * BCrypt is an infrastructure detail — it lives in the adapter, not here.
 */
public interface PasswordEncoderPort {

    String encode(String rawPassword);

    boolean matches(String rawPassword, String encodedPassword);
}

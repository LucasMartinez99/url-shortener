package com.urlshortener.domain.port.out;

/**
 * OUTPUT PORT for short-code generation.
 *
 * Defining this as a port lets us swap the generation strategy
 * (random, sequential, hash-based) without touching any business logic.
 */
public interface ShortCodeGeneratorPort {

    String generate();
}

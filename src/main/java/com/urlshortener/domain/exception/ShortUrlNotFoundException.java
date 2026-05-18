package com.urlshortener.domain.exception;

public class ShortUrlNotFoundException extends DomainException {

    public ShortUrlNotFoundException(String code) {
        super("Short URL not found: " + code);
    }
}

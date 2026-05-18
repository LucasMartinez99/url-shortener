package com.urlshortener.domain.exception;

public class UrlExpiredException extends DomainException {

    public UrlExpiredException(String code) {
        super("URL has expired: " + code);
    }
}

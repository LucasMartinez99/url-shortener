package com.urlshortener.domain.exception;

public class InvalidAliasException extends DomainException {

    public InvalidAliasException(String message) {
        super(message);
    }
}

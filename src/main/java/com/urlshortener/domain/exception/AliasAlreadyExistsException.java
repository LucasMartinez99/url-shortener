package com.urlshortener.domain.exception;

public class AliasAlreadyExistsException extends DomainException {

    public AliasAlreadyExistsException(String alias) {
        super("Alias already in use: " + alias);
    }
}

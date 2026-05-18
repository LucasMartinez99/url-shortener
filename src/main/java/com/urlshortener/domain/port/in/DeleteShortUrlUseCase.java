package com.urlshortener.domain.port.in;

import java.util.UUID;

public interface DeleteShortUrlUseCase {

    void delete(UUID shortUrlId, UUID requestingUserId);
}

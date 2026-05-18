package com.urlshortener.domain.port.in;

import com.urlshortener.domain.model.ShortUrl;

import java.util.UUID;

public interface GetUrlAnalyticsUseCase {

    ShortUrl getAnalytics(UUID shortUrlId, UUID requestingUserId);
}

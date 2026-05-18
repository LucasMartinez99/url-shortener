package com.urlshortener.domain.port.in;

import com.urlshortener.domain.model.ShortUrl;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface GetUserUrlsUseCase {

    Page<ShortUrl> getUserUrls(UUID userId, Pageable pageable);
}

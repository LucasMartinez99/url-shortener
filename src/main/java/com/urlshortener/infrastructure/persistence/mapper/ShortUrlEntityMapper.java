package com.urlshortener.infrastructure.persistence.mapper;

import com.urlshortener.domain.model.ShortUrl;
import com.urlshortener.infrastructure.persistence.entity.ShortUrlEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ShortUrlEntityMapper {

    ShortUrl toDomain(ShortUrlEntity entity);

    ShortUrlEntity toEntity(ShortUrl domain);
}

package com.urlshortener.infrastructure.persistence.mapper;

import com.urlshortener.domain.model.User;
import com.urlshortener.infrastructure.persistence.entity.UserEntity;
import org.mapstruct.Mapper;

/**
 * MapStruct generates the implementation at compile time.
 * No reflection at runtime — faster than manual mapping and type-safe.
 */
@Mapper(componentModel = "spring")
public interface UserEntityMapper {

    User toDomain(UserEntity entity);

    UserEntity toEntity(User domain);
}

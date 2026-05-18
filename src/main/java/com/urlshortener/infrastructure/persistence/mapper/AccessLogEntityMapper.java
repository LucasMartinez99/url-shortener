package com.urlshortener.infrastructure.persistence.mapper;

import com.urlshortener.domain.model.AccessLog;
import com.urlshortener.infrastructure.persistence.entity.AccessLogEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AccessLogEntityMapper {

    AccessLog toDomain(AccessLogEntity entity);

    AccessLogEntity toEntity(AccessLog domain);
}

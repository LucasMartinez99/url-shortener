package com.urlshortener.infrastructure.persistence.repository;

import com.urlshortener.infrastructure.persistence.entity.AccessLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AccessLogJpaRepository extends JpaRepository<AccessLogEntity, UUID> {
}

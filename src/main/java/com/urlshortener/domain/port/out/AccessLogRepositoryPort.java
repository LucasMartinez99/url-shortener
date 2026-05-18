package com.urlshortener.domain.port.out;

import com.urlshortener.domain.model.AccessLog;

public interface AccessLogRepositoryPort {

    void save(AccessLog accessLog);
}

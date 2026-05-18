package com.urlshortener.domain.port.in;

public interface RedirectUrlUseCase {

    String resolveUrl(ResolveUrlCommand command);

    record ResolveUrlCommand(String code, String ipAddress, String userAgent) {}
}

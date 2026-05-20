package com.urlshortener.infrastructure.security;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimitFilter extends OncePerRequestFilter {

    @Value("${app.rate-limit.enabled:true}")
    private boolean enabled;

    // Per-IP buckets for each limit tier
    private final ConcurrentHashMap<String, Bucket> authBuckets     = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Bucket> redirectBuckets = new ConcurrentHashMap<>();

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !enabled;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest  request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain         filterChain
    ) throws ServletException, IOException {

        String path = request.getRequestURI();
        String ip   = extractClientIp(request);

        Bucket bucket = resolveBucket(path, ip);

        if (bucket != null && !bucket.tryConsume(1)) {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.getWriter().write("{\"status\":429,\"error\":\"Too Many Requests\",\"message\":\"Rate limit exceeded. Please slow down.\"}");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private Bucket resolveBucket(String path, String ip) {
        if (path.startsWith("/api/v1/auth/")) {
            // 10 requests per minute on auth endpoints
            return authBuckets.computeIfAbsent(ip, k -> newBucket(10, Duration.ofMinutes(1)));
        }
        if (!path.startsWith("/api/") && !path.startsWith("/swagger") && !path.startsWith("/v3/")) {
            // 60 requests per minute on redirect (/{code}) endpoint
            return redirectBuckets.computeIfAbsent(ip, k -> newBucket(60, Duration.ofMinutes(1)));
        }
        return null;
    }

    private Bucket newBucket(long capacity, Duration refillPeriod) {
        Bandwidth limit = Bandwidth.builder()
                .capacity(capacity)
                .refillGreedy(capacity, refillPeriod)
                .build();
        return Bucket.builder().addLimit(limit).build();
    }

    private String extractClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        return (forwarded != null) ? forwarded.split(",")[0].trim() : request.getRemoteAddr();
    }
}

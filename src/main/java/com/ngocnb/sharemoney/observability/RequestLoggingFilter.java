package com.ngocnb.sharemoney.observability;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@Slf4j
public class RequestLoggingFilter extends OncePerRequestFilter {

    public static final String REQUEST_ID_KEY = "requestId";
    private static final String REQUEST_ID_HEADER = "X-Request-Id";
    private static final Set<String> SENSITIVE_PARAM_NAMES = Set.of(
            "authorization",
            "access_token",
            "accessToken",
            "refresh_token",
            "refreshToken",
            "token",
            "password",
            "secret",
            "api_key",
            "apiKey",
            "key",
            "cookie",
            "credential",
            "redis_password",
            "database_url"
    );

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        long startNanos = System.nanoTime();
        String requestId = resolveRequestId(request);
        String method = request.getMethod();
        String path = request.getRequestURI();
        String query = buildSanitizedQuery(request);
        String remoteIp = resolveRemoteIp(request);

        MDC.put(REQUEST_ID_KEY, requestId);
        response.setHeader(REQUEST_ID_HEADER, requestId);

        try {
            log.info(
                    "HTTP {} {}{} started remoteIp={}",
                    method,
                    path,
                    query,
                    remoteIp
            );

            filterChain.doFilter(request, response);
        } catch (Exception exception) {
            log.error(
                    "{} {}{} failed: {}",
                    method,
                    path,
                    query,
                    ExceptionLogUtils.describe(exception)
            );

            Throwable rootCause = ExceptionLogUtils.rootCause(exception);
            if (rootCause != null && rootCause != exception) {
                log.error("Root cause: {}", ExceptionLogUtils.describe(rootCause));
            }

            throw exception;
        } finally {
            long durationMs = (System.nanoTime() - startNanos) / 1_000_000;

            log.info(
                    "HTTP {} {}{} completed status={} duration={}ms",
                    method,
                    path,
                    query,
                    response.getStatus(),
                    durationMs
            );

            MDC.remove(REQUEST_ID_KEY);
        }
    }

    private String resolveRequestId(HttpServletRequest request) {
        String requestId = request.getHeader(REQUEST_ID_HEADER);
        if (requestId == null || requestId.isBlank()) {
            return UUID.randomUUID().toString();
        }
        return ExceptionLogUtils.sanitize(requestId.trim());
    }

    private String buildSanitizedQuery(HttpServletRequest request) {
        if (request.getParameterMap().isEmpty()) {
            return "";
        }

        UriComponentsBuilder builder = UriComponentsBuilder.newInstance();
        request.getParameterMap().forEach((name, values) -> {
            String value = isSensitiveParam(name)
                    ? "****"
                    : String.join(",", values);
            builder.queryParam(name, value);
        });

        String query = builder.build().encode().toUriString();
        return query.isBlank() ? "" : query;
    }

    private boolean isSensitiveParam(String name) {
        return SENSITIVE_PARAM_NAMES.stream()
                .anyMatch(sensitiveName -> sensitiveName.equalsIgnoreCase(name));
    }

    private String resolveRemoteIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return ExceptionLogUtils.sanitize(
                    List.of(forwardedFor.split(",")).get(0).trim()
            );
        }

        String realIp = request.getHeader("X-Real-IP");
        if (realIp != null && !realIp.isBlank()) {
            return ExceptionLogUtils.sanitize(realIp.trim());
        }

        return request.getRemoteAddr();
    }
}

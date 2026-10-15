package com.waterbill.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.bucket4j.ConsumptionProbe;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
public class RateLimitingFilter extends OncePerRequestFilter {

    private final RateLimitingService rateLimitingService;
    private final ObjectMapper objectMapper;

    @Value("${app.security.rate-limiting.enabled:true}")
    private boolean rateLimitingEnabled;

    public RateLimitingFilter(RateLimitingService rateLimitingService, ObjectMapper objectMapper) {
        this.rateLimitingService = rateLimitingService;
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        if (!rateLimitingEnabled) {
            filterChain.doFilter(request, response);
            return;
        }

        String path = request.getRequestURI();
        String clientIp = extractClientIp(request);

        // Apply strict rate limiting to public login and registration endpoints
        if (isAuthEndpoint(path)) {
            ConsumptionProbe probe = rateLimitingService.probeAuthEndpoint(clientIp);
            if (probe.isConsumed()) {
                response.addHeader("X-Rate-Limit-Remaining", String.valueOf(probe.getRemainingTokens()));
                filterChain.doFilter(request, response);
            } else {
                long retryAfterSec = Math.max(1, probe.getNanosToWaitForRefill() / 1_000_000_000L);
                response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                response.addHeader("X-Rate-Limit-Retry-After-Seconds", String.valueOf(retryAfterSec));
                response.addHeader("Retry-After", String.valueOf(retryAfterSec));

                Map<String, Object> errorBody = new HashMap<>();
                errorBody.put("timestamp", OffsetDateTime.now().toString());
                errorBody.put("status", HttpStatus.TOO_MANY_REQUESTS.value());
                errorBody.put("error", "Too Many Requests");
                errorBody.put("message", "Rate limit exceeded. Maximum 5 requests per minute allowed on authentication endpoints.");
                errorBody.put("retryAfterSeconds", retryAfterSec);

                response.getWriter().write(objectMapper.writeValueAsString(errorBody));
            }
            return;
        }

        filterChain.doFilter(request, response);
    }

    private boolean isAuthEndpoint(String path) {
        return path.startsWith("/api/auth/") ||
               path.startsWith("/api/login") ||
               path.equals("/login") ||
               path.startsWith("/api/register") ||
               path.equals("/register") ||
               path.startsWith("/sendOtp") ||
               path.startsWith("/api/sendOtp") ||
               path.startsWith("/validateOtp") ||
               path.startsWith("/api/validateOtp") ||
               path.startsWith("/api/otp");
    }

    private String extractClientIp(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null || xfHeader.isBlank()) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0].trim();
    }
}

package com.waterbill.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RateLimitingService {

    private final Map<String, Bucket> authBuckets = new ConcurrentHashMap<>();
    private final Map<String, Bucket> generalBuckets = new ConcurrentHashMap<>();

    @Value("${app.security.rate-limiting.auth-limit-per-minute:5}")
    private long authLimitPerMinute;

    @Value("${app.security.rate-limiting.general-limit-per-minute:60}")
    private long generalLimitPerMinute;

    public ConsumptionProbe probeAuthEndpoint(String clientKey) {
        Bucket bucket = authBuckets.computeIfAbsent(clientKey, this::createAuthBucket);
        return bucket.tryConsumeAndReturnRemaining(1);
    }

    public ConsumptionProbe probeGeneralEndpoint(String clientKey) {
        Bucket bucket = generalBuckets.computeIfAbsent(clientKey, this::createGeneralBucket);
        return bucket.tryConsumeAndReturnRemaining(1);
    }

    private Bucket createAuthBucket(String key) {
        Bandwidth limit = Bandwidth.builder()
                .capacity(authLimitPerMinute)
                .refillGreedy(authLimitPerMinute, Duration.ofMinutes(1))
                .build();
        return Bucket.builder().addLimit(limit).build();
    }

    private Bucket createGeneralBucket(String key) {
        Bandwidth limit = Bandwidth.builder()
                .capacity(generalLimitPerMinute)
                .refillGreedy(generalLimitPerMinute, Duration.ofMinutes(1))
                .build();
        return Bucket.builder().addLimit(limit).build();
    }
}

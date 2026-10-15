package com.waterbill.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

@Configuration
@EnableCaching
public class RedisCacheConfig implements CachingConfigurer {

    private static final Logger log = LoggerFactory.getLogger(RedisCacheConfig.class);

    public static final String CACHE_TARIFFS = "tariffs";
    public static final String CACHE_DASHBOARD_METRICS = "dashboardMetrics";

    @Bean
    @Primary
    @ConditionalOnProperty(name = "spring.cache.type", havingValue = "redis", matchIfMissing = true)
    public CacheManager redisCacheManager(RedisConnectionFactory connectionFactory) {
        try {
            // Test connection to Redis before constructing RedisCacheManager
            try (RedisConnection connection = connectionFactory.getConnection()) {
                String pong = connection.ping();
                log.info("Redis connection established successfully: ping response={}", pong);
            }

            RedisCacheConfiguration cacheConfig = RedisCacheConfiguration.defaultCacheConfig()
                    .entryTtl(Duration.ofMinutes(60))
                    .disableCachingNullValues()
                    .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                    .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()));

            return RedisCacheManager.builder(connectionFactory)
                    .cacheDefaults(cacheConfig)
                    .withCacheConfiguration(CACHE_TARIFFS, cacheConfig.entryTtl(Duration.ofHours(2)))
                    .withCacheConfiguration(CACHE_DASHBOARD_METRICS, cacheConfig.entryTtl(Duration.ofMinutes(10)))
                    .build();
        } catch (Exception ex) {
            log.warn("Redis server is offline or unreachable ({}). Seamlessly falling back to in-memory ConcurrentMapCacheManager.", ex.getMessage());
            return fallbackCacheManager();
        }
    }

    @Bean(name = "fallbackCacheManager")
    public CacheManager fallbackCacheManager() {
        return new ConcurrentMapCacheManager(CACHE_TARIFFS, CACHE_DASHBOARD_METRICS);
    }

    @Override
    public CacheErrorHandler errorHandler() {
        return new CacheErrorHandler() {
            @Override
            public void handleCacheGetError(RuntimeException exception, Cache cache, Object key) {
                log.warn("Redis cache GET failed for key [{}] in cache [{}]: {}. Proceeding with source data execution.", key, cache.getName(), exception.getMessage());
            }

            @Override
            public void handleCachePutError(RuntimeException exception, Cache cache, Object key, Object value) {
                log.warn("Redis cache PUT failed for key [{}] in cache [{}]: {}. Continuing without cache write.", key, cache.getName(), exception.getMessage());
            }

            @Override
            public void handleCacheEvictError(RuntimeException exception, Cache cache, Object key) {
                log.warn("Redis cache EVICT failed for key [{}] in cache [{}]: {}.", key, cache.getName(), exception.getMessage());
            }

            @Override
            public void handleCacheClearError(RuntimeException exception, Cache cache) {
                log.warn("Redis cache CLEAR failed in cache [{}]: {}.", cache.getName(), exception.getMessage());
            }
        };
    }
}

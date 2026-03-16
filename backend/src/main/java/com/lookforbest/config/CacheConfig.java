package com.lookforbest.config;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

import java.time.Duration;
import java.util.Map;

/**
 * Redis 缓存配置：为各业务场景设置差异化 TTL
 */
@Configuration
@EnableCaching
public class CacheConfig {

    // 缓存名称常量
    public static final String ROBOTS_LIST   = "robots:list";
    public static final String ROBOTS_DETAIL = "robots:detail";
    public static final String ROBOTS_SIMILAR = "robots:similar";
    public static final String CATEGORIES    = "categories";
    public static final String MANUFACTURERS = "manufacturers";
    public static final String DOMAINS       = "domains";
    public static final String SEARCH_SUGGEST = "search:suggest";

    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        mapper.addMixIn(Object.class, HibernateProxyMixin.class);
        GenericJackson2JsonRedisSerializer jsonSerializer = new GenericJackson2JsonRedisSerializer(mapper);

        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(jsonSerializer))
                .disableCachingNullValues();

        Map<String, RedisCacheConfiguration> cacheConfigs = Map.of(
                ROBOTS_LIST,    defaultConfig.entryTtl(Duration.ofMinutes(10)),
                ROBOTS_DETAIL,  defaultConfig.entryTtl(Duration.ofMinutes(30)),
                ROBOTS_SIMILAR, defaultConfig.entryTtl(Duration.ofMinutes(15)),
                CATEGORIES,     defaultConfig.entryTtl(Duration.ofHours(6)),
                MANUFACTURERS,  defaultConfig.entryTtl(Duration.ofHours(6)),
                DOMAINS,        defaultConfig.entryTtl(Duration.ofHours(6)),
                SEARCH_SUGGEST, defaultConfig.entryTtl(Duration.ofMinutes(5))
        );

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig.entryTtl(Duration.ofMinutes(30)))
                .withInitialCacheConfigurations(cacheConfigs)
                .build();
    }

    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    abstract static class HibernateProxyMixin {}
}

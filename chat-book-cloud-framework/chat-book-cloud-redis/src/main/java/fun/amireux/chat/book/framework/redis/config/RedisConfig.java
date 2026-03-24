package fun.amireux.chat.book.framework.redis.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

@AutoConfiguration(after = RedisAutoConfiguration.class)
@ConditionalOnClass(RedisOperations.class)
@ConditionalOnBean(RedisConnectionFactory.class)
@EnableCaching
public class RedisConfig {

    @Bean("objectRedisTemplate")
    @Primary
    @ConditionalOnMissingBean(name = "objectRedisTemplate")
    public RedisTemplate<String, Object> objectRedisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        StringRedisSerializer keySerializer = new StringRedisSerializer();
        GenericJackson2JsonRedisSerializer valueSerializer = createRedisSerializer();

        template.setKeySerializer(keySerializer);
        template.setHashKeySerializer(keySerializer);
        template.setValueSerializer(valueSerializer);
        template.setHashValueSerializer(valueSerializer);
        template.afterPropertiesSet();
        return template;
    }

    @Bean
    @ConditionalOnMissingBean(CacheManager.class)
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        GenericJackson2JsonRedisSerializer serializer = createRedisSerializer();

        RedisCacheConfiguration defaultCacheConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(30))
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(serializer))
                .disableCachingNullValues();

        RedisCacheConfiguration articleCacheConfig = defaultCacheConfig.entryTtl(Duration.ofHours(1));
        RedisCacheConfiguration userCacheConfig = defaultCacheConfig.entryTtl(Duration.ofMinutes(15));
        RedisCacheConfiguration tagListCacheConfig = defaultCacheConfig.entryTtl(Duration.ofMinutes(30));
        RedisCacheConfiguration reviewListCacheConfig = defaultCacheConfig.entryTtl(Duration.ofMinutes(30));
        RedisCacheConfiguration followStatCacheConfig = defaultCacheConfig.entryTtl(Duration.ofMinutes(15));
        RedisCacheConfiguration articleListCacheConfig = defaultCacheConfig.entryTtl(Duration.ofMinutes(5));

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultCacheConfig)
                .withCacheConfiguration("articleCache", articleCacheConfig)
                .withCacheConfiguration("userCache", userCacheConfig)
                .withCacheConfiguration("tagListCache", tagListCacheConfig)
                .withCacheConfiguration("reviewListCache", reviewListCacheConfig)
                .withCacheConfiguration("followStatCache", followStatCacheConfig)
                .withCacheConfiguration("articleListCache", articleListCacheConfig)
                .build();
    }

    GenericJackson2JsonRedisSerializer createRedisSerializer() {
        return new GenericJackson2JsonRedisSerializer().configure(objectMapper -> {
            objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
            objectMapper.registerModule(new JavaTimeModule());
            objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        });
    }
}

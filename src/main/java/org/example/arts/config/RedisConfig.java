package org.example.arts.config;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.example.arts.dtos.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

import java.time.Duration;
import java.util.List;

@Configuration
public class RedisConfig {
    @Value("${redis.host}")
    private String redisHost;

    @Value("${redis.port}")
    private int redisPort;

    @Bean
    public LettuceConnectionFactory redisConnectionFactory() {
        RedisStandaloneConfiguration configuration = new RedisStandaloneConfiguration(redisHost, redisPort);
        return new LettuceConnectionFactory(configuration);
    }

    private ObjectMapper createBaseObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        SimpleModule module = new SimpleModule();
        module.addDeserializer(PageImpl.class, new PageImplDeserializer());
        mapper.registerModule(module);
        return mapper;
    }

    @Primary
    @Bean("cacheObjectMapper")
    public ObjectMapper cacheObjectMapper() {
        ObjectMapper mapper = createBaseObjectMapper();
        mapper.activateDefaultTyping(
                LaissezFaireSubTypeValidator.instance,
                ObjectMapper.DefaultTyping.NON_FINAL,
                com.fasterxml.jackson.annotation.JsonTypeInfo.As.PROPERTY
        );
        return mapper;
    }

    @Bean("specificObjectMapper")
    public ObjectMapper specificObjectMapper() {
        return createBaseObjectMapper();
    }

    @Bean
    public ObjectMapper httpObjectMapper() {
        return createBaseObjectMapper();
    }

    @Bean
    public MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter(
            @Qualifier("httpObjectMapper") ObjectMapper httpObjectMapper) {
        return new MappingJackson2HttpMessageConverter(httpObjectMapper);
    }


    @Bean
    public RedisCacheManager cacheManager(
            LettuceConnectionFactory redisConnectionFactory,
            @Qualifier("cacheObjectMapper") ObjectMapper cacheObjectMapper,
            @Qualifier("specificObjectMapper") ObjectMapper specificObjectMapper) {

        RedisCacheConfiguration cacheConfig = createRedisCacheConfiguration(Duration.ofMinutes(10), new GenericJackson2JsonRedisSerializer(cacheObjectMapper)).disableCachingNullValues();

        return RedisCacheManager.builder(redisConnectionFactory)
                .cacheDefaults(cacheConfig)
                .withCacheConfiguration("arts", specificCacheConfig(Duration.ofMinutes(120), specificObjectMapper, ArtDto.class))
                .withCacheConfiguration("comments", specificPageCacheConfig(Duration.ofMinutes(120), specificObjectMapper, CommentDto.class))
                .withCacheConfiguration("arts-search", specificPageCacheConfig(Duration.ofMinutes(15), specificObjectMapper, ArtCardDto.class))
                .withCacheConfiguration("arts-author", specificPageCacheConfig(Duration.ofMinutes(30), specificObjectMapper, ArtCardDto.class))
                .withCacheConfiguration("tags", specificListCacheConfig(Duration.ofMinutes(120), specificObjectMapper, TagDto.class))
                .withCacheConfiguration("social-network", specificListCacheConfig(Duration.ofMinutes(60), specificObjectMapper, SocialNetworkDto.class))
                .withCacheConfiguration("user", specificCacheConfig(Duration.ofMinutes(120), specificObjectMapper, UserDto.class))
                .build();
    }

    private RedisCacheConfiguration createRedisCacheConfiguration(Duration duration, RedisSerializer<?> valueSerializer) {
        return RedisCacheConfiguration
                .defaultCacheConfig()
                .entryTtl(duration)
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(valueSerializer));
    }

    private <T> RedisCacheConfiguration specificCacheConfig(Duration duration, ObjectMapper objectMapper, Class<T> type) {
        Jackson2JsonRedisSerializer<T> jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer<>(objectMapper, type);
        return createRedisCacheConfiguration(duration, jackson2JsonRedisSerializer);
    }

    private RedisCacheConfiguration specificPageCacheConfig(Duration duration, ObjectMapper objectMapper, Class<?> contentType) {
        JavaType type = objectMapper.getTypeFactory().constructParametricType(PageImpl.class, contentType);
        Jackson2JsonRedisSerializer<Page<?>> jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer<>(objectMapper, type);
        return createRedisCacheConfiguration(duration, jackson2JsonRedisSerializer);
    }

    private <T> RedisCacheConfiguration specificListCacheConfig(Duration duration, ObjectMapper objectMapper, Class<T> elementType) {
        JavaType type = objectMapper.getTypeFactory().constructCollectionType(List.class, elementType);
        Jackson2JsonRedisSerializer<List<T>> jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer<>(objectMapper, type);
        return createRedisCacheConfiguration(duration, jackson2JsonRedisSerializer);
    }
}
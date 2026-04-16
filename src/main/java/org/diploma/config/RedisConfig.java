//package org.diploma.cofing;
//
//import org.springframework.beans.factory.annotation.Qualifier;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.data.redis.connection.RedisConnectionFactory;
//import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
//import org.springframework.data.redis.core.RedisTemplate;
//
//@Configuration
//public class RedisConfig {
//
////    @Bean
////    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {
////        return RedisCacheManager.create(connectionFactory);
////    }
////
////    @Bean
////    JedisConnectionFactory jedisConnectionFactory() {
////        return new JedisConnectionFactory();
////    }
////
////    @Bean
////    public RedisTemplate<String, Object> redisTemplate() {
////        RedisTemplate<String, Object> template = new RedisTemplate<>();
////        template.setConnectionFactory(jedisConnectionFactory());
////        template.setKeySerializer(new StringRedisSerializer());
////        template.setValueSerializer(new StringRedisSerializer());
////        template.afterPropertiesSet();
////        return template;
////    }
//
////    @Bean
////    public RedisCacheConfiguration cacheConfiguration() {
////        return RedisCacheConfiguration.defaultCacheConfig()
////                .entryTtl(Duration.ofMinutes(60))
////                .disableCachingNullValues()
////                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericToStringSerializer<String>(String.class)));
////    }
////
////    @Bean
////    public RedisCacheManagerBuilderCustomizer redisCacheManagerBuilderCustomizer() {
////        return (builder) -> builder
////                .withCacheConfiguration("itemCache",
////                        RedisCacheConfiguration.defaultCacheConfig().entryTtl(Duration.ofMinutes(10)))
////                .withCacheConfiguration("customerCache",
////                        RedisCacheConfiguration.defaultCacheConfig().entryTtl(Duration.ofMinutes(5)));
////    }
//
//    @Bean(name = "connectionFactory")
//    public LettuceConnectionFactory connectionFactory() {
//        return new LettuceConnectionFactory();
//    }
//
//    @Bean(name = "redisTemplate")
//    public RedisTemplate<String, String> redisTemplate(@Qualifier("connectionFactory") RedisConnectionFactory connectionFactory) {
//        RedisTemplate<String, String> template = new RedisTemplate<>();
//        template.setConnectionFactory(connectionFactory);
//        return template;
//    }
//}

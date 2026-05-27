package org.diploma.listerner;

import lombok.extern.slf4j.Slf4j;
import org.diploma.repository.RedisOffsetRepository;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicLong;


@Slf4j
@Service
public class KafkaMessageListener {

    private final AtomicLong globalCounter;
    private final AtomicLong localCounter;

    private final CacheManager cacheManager;
    private final RedisOffsetRepository redisOffsetRepository;
    private final Cache cache;

    public KafkaMessageListener(CacheManager cacheManager, RedisOffsetRepository redisOffsetRepository) {
        this.cacheManager = cacheManager;
        this.redisOffsetRepository = redisOffsetRepository;
        this.localCounter = new AtomicLong(0);
        this.globalCounter = new AtomicLong(redisOffsetRepository.getRedisOffset());
        this.cache = cacheManager.getCache("testCache");
    }

    @KafkaListener(topics = "databaseServerNameTest.public.example")
    public void read(@Payload String message) {
        if (globalCounter.get() == Long.MAX_VALUE) {
            redisOffsetRepository.updateRedisOffset();
        }

        String redisFullOffset = String.valueOf(globalCounter.get()) + "." +
                String.valueOf(localCounter.getAndIncrement());

        if (cache != null)
            cache.put(redisFullOffset, message);
    }
}

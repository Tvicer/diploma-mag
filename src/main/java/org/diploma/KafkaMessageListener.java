package org.diploma;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicInteger;


@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaMessageListener {

    private final AtomicInteger counter = new AtomicInteger(0);

    private final CacheManager cacheManager;

    @KafkaListener(topics = "databaseServerNameTest.public.example")
    public void read(@Payload String message) {
//        log.info("Message {} received", message);
        Cache cache = cacheManager.getCache("testCache");

        if (cache != null)
            cache.put(counter.getAndIncrement(), message);
    }
}

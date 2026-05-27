package org.diploma.repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class RedisOffsetRepository {

    private final JdbcTemplate jdbcTemplate;

    public Long getRedisOffset() {
        return jdbcTemplate.queryForObject(
                "SELECT offset_value FROM public.redis_offset WHERE id = 'redisCahce1';",
                Long.class
        );
    }

    public void updateRedisOffset() {
        jdbcTemplate.queryForList("UPDATE public.redis_offset SET offset_value = offset_value + 1 WHERE id = 'redisCahce1';");
    }
}

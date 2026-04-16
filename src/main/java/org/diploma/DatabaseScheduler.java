package org.diploma;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class DatabaseScheduler {

    private final JdbcTemplate jdbcTemplate;

    private final KafkaTemplate<String, String> kafkaTemplate;

//    @Scheduled(fixedDelay = 1000)
    public void execute() {
        List<Map<String, @Nullable Object>> result = jdbcTemplate.queryForList("select * from example values (id, name)");
        log.info(Arrays.stream(result.toArray()).toList().toString());
    }

}

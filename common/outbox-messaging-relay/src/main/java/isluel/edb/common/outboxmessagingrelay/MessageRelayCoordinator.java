package isluel.edb.common.outboxmessagingrelay;

import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.connection.StringRedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class MessageRelayCoordinator {
    private final StringRedisTemplate redisTemplate;

    //
    @Value("${spring.application.name}")
    private String applicationName;

    private final String APP_ID = UUID.randomUUID().toString();

    // outbox 모듈을 각 Service 에서 실행하였을 경우 coordinator가 독립적인 키로
    // 동작하도록 하여 ApplicationName 별로 실행중인 Application을 저장하도록함.
    private String generateKey() {
        return "message-relay-coordinator::app-list::%s".formatted(applicationName);
    }

    private final int PING_INTERVAL_SECONDS = 3;
    private final int PING_FAILURE_THRESHOLD = 3;

    // 현재 Application에 해대 할당된 목록을 반환
    public AssignedShard assignedShards() {
        return AssignedShard.of(APP_ID, findAppIds(), MessageRelayConstants.SHARD_COUNT);
    }

    private List<String> findAppIds() {
        return redisTemplate.opsForZSet().reverseRange(generateKey(), 0, -1)
                .stream().sorted().toList();
    }

    @Scheduled(fixedDelay = PING_INTERVAL_SECONDS, timeUnit = TimeUnit.SECONDS)
    public void ping() {
        redisTemplate.executePipelined( (RedisCallback<?>) action -> {
            StringRedisConnection connection = (StringRedisConnection) action;
            String key = generateKey();
            // Application ID를 추가
            connection.zAdd(key, Instant.now().toEpochMilli(), APP_ID);
            // 스코어 기준으로 3번 실패하면 삭제
            connection.zRemRangeByScore(
                    key, Double.NEGATIVE_INFINITY
                    // PING_INTERVAL_SECONDS * PING_FAILURE_THRESHOLD 이상인 항목은 삭제
                    , Instant.now().minusSeconds(PING_INTERVAL_SECONDS * PING_FAILURE_THRESHOLD).toEpochMilli()
            );
            return null;
        });
    }

    // Application 종료시
    // 자신의 Application ID에 해당되는 데이터 삭제
    @PreDestroy
    public void leave() {
        redisTemplate.opsForZSet().remove(generateKey(), APP_ID);
    }
}

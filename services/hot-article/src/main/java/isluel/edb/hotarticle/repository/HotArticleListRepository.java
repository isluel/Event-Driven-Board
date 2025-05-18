package isluel.edb.hotarticle.repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.StringRedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Repository
public class HotArticleListRepository {
    private final StringRedisTemplate redisTemplate;

    // hot-article::list::{yyyyMMdd}
    private static final String KEY_FORMAT = "hot-article::list::%s";

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    // Redis에 데이터 저장
    // article id, 저장 시간, 인기글 점수 계산 결과 값, 가지고 있을 인기글 정보의 최대 개수(10개), TTL
    public void add(Long articleId, LocalDateTime time, Long score, Long limit, Duration ttl) {
        redisTemplate.executePipelined((RedisCallback<?>) action -> {
            StringRedisConnection connection = (StringRedisConnection) action;
            String key = generateKey(time);
            // 키 추가 및 업데이트
            connection.zAdd(key, score, String.valueOf(articleId));
            // 상위 10개만 남기고 나머지는 삭제
            connection.zRemRange(key, 0,  -limit - 1);
            connection.expire(key, ttl.toSeconds());
            return null;
        });
    }

    // 게시글 삭제시 hot article 도 같이 삭제가 되도록 한다.
    public void remove(Long articleId, LocalDateTime time) {
        redisTemplate.opsForZSet().remove(generateKey(time), String.valueOf(articleId));
    }

    private String generateKey(LocalDateTime time) {
        return generateKey(TIME_FORMATTER.format(time));
    }

    private String generateKey(String time) {
        return KEY_FORMAT.formatted(time);
    }

    // 입력한 날짜에 대한 인기글의 articleId를 모두 조회
    public List<Long> readAll(String dateString) {
        return redisTemplate.opsForZSet()
                .reverseRangeWithScores(generateKey(dateString), 0 ,-1)
                .stream()
                .peek((ZSetOperations.TypedTuple<String> tuple)
                        -> log.info("[HotArticleListRepository.readAll] articleId={}, score={}", tuple.getValue(), tuple.getScore()))
                .map(ZSetOperations.TypedTuple::getValue)
                .map(Long::valueOf)
                .toList();
    }
}

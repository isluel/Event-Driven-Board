package isluel.edb.articleread.cache;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@RequiredArgsConstructor
public class OptimizedCacheLockProvider {
    private final StringRedisTemplate redisTemplate;

    private static final String KEY_PREFIX = "optimized-cache-lock::";
    // Lock은 캐시 원본 데이터에 대해서 갱신하기 위해 3초정도면 충분하다고 생각
    private static final Duration LOCK_TTL = Duration.ofSeconds(3);

    // lock을 잡는 메서드
    // 성공하면 true 반환
    public boolean lock(String key) {
        return redisTemplate.opsForValue().setIfAbsent(generateLockKey(key), "", LOCK_TTL);
    }

    public void unlock(String key) {
        redisTemplate.delete(generateLockKey(key));
    }

    private String generateLockKey(String key) {
        return KEY_PREFIX + key;
    }
}

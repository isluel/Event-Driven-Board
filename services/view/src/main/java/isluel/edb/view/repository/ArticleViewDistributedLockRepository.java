package isluel.edb.view.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;

@Repository
@RequiredArgsConstructor
public class ArticleViewDistributedLockRepository {
    private final StringRedisTemplate redisTemplate;

    // view::article::{aritcle_id}::user::{user_id}::lock
    private static final String KEY_FORMAT = "view::article::%s::user::%s::lock";

    public boolean lock(Long articleId, Long userId, Duration duration) {
        String key = generateKey(articleId, userId);
        return redisTemplate.opsForValue().setIfAbsent(key, "", duration);
    }

    private String generateKey(Long articleId, Long userId) {
        return KEY_FORMAT.formatted(articleId, userId);
    }
}

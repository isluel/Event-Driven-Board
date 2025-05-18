package isluel.edb.hotarticle.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;

@Repository
@RequiredArgsConstructor
public class ArticleLikeCountRepository {
    private final StringRedisTemplate redisTemplate;

    // hot-article::article::{articleId}::like-count
    private static final String KEY_FORMAT = "hot-article::article::%s::like-count";

    public void createOrUpdate(Long article, Long likeCount, Duration ttl) {
        redisTemplate.opsForValue().set(generateKey(article), String.valueOf(likeCount), ttl);
    }

    public Long read(Long article) {
        String result = redisTemplate.opsForValue().get(generateKey(article));
        return result == null ? 0L : Long.parseLong(result);
    }

    private String generateKey(Long articleId) {
        return KEY_FORMAT.formatted(articleId);
    }
}

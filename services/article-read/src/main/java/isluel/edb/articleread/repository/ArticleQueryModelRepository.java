package isluel.edb.articleread.repository;

import isluel.edb.common.dataserialiser.DataSerializer;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class ArticleQueryModelRepository {
    private final StringRedisTemplate redisTemplate;

    // article-read::article::{articleId}
    private static String KEY_FORMAT = "article-read::article::%s";

    // 항상 모든 데이터를 저장하지 않고, 오래된거는 삭제되도록 한다.
    public void create(ArticleQueryModel articleQueryModel, Duration ttl) {
        redisTemplate.opsForValue()
                .set(generateKey(articleQueryModel), DataSerializer.serialize(articleQueryModel), ttl);
    }

    // 데이터 업데이트, TTL을 업데이트 하지 않도록한다.. 신규 값만 update
    public void update(ArticleQueryModel articleQueryModel) {
        redisTemplate.opsForValue().setIfAbsent(generateKey(articleQueryModel), DataSerializer.serialize(articleQueryModel));
    }

    public void delete(Long articleId) {
        redisTemplate.delete(generateKey(articleId));
    }

    public Optional<ArticleQueryModel> read(Long articleId) {
        return Optional.ofNullable(
                redisTemplate.opsForValue().get(generateKey(articleId))
        ).map(json -> DataSerializer.deserialize(json, ArticleQueryModel.class));
    }

    private String generateKey(ArticleQueryModel articleQueryModel) {
        return generateKey(articleQueryModel.getArticleId());
    }

    private String generateKey(Long articleId) {
        return KEY_FORMAT.formatted(articleId);
    }

    public Map<Long, ArticleQueryModel> readAll(List<Long> articleIds) {
            var keyList = articleIds.stream().map(this::generateKey).toList();
            return redisTemplate.opsForValue().multiGet(keyList)
                    .stream().filter(Objects::nonNull)
                    .map(json -> DataSerializer.deserialize(json, ArticleQueryModel.class))
                    .collect(Collectors.toMap(ArticleQueryModel::getArticleId, Function.identity()));

    }
}

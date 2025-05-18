package isluel.edb.articleread.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Range;
import org.springframework.data.redis.connection.Limit;
import org.springframework.data.redis.connection.StringRedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class ArticleIdListRepository {
    private final StringRedisTemplate redisTemplate;

    // article-read::board::{board_id}::article-list
    private static String KEY_FORMAT = "article-read::board::%s::article-list";

    // limit : Redis에 저장할 게시글 목록 갯수
    public void add(Long boardId, Long articleId, Long limit) {
        redisTemplate.executePipelined((RedisCallback<?>) action -> {
            StringRedisConnection redisConnection = (StringRedisConnection) action;
            String key = generateKey(boardId);
            // score 를 0 으로 고정,
            // value 값을 padding 된 값으로 사용.
            // score 가 double 이라서, id 는 long 값이어서 데이터 유실이 발생.
            // 동일한 score는 value 값에 따라서 정렬이 됨.
            redisConnection.zAdd(key, 0, toPaddingString(articleId));
            // limit 개수만 남기기
            redisConnection.zRemRange(key, 0, - limit -1);
            return null;
        });
    }

    public void delete(Long boardId, Long articleId) {
        redisTemplate.opsForZSet().remove(generateKey(boardId), toPaddingString(articleId));
    }

    // page랑 pageSize로 offset, limit를 계산하여 전달
    public List<Long> readAll(Long boardId, Long offset, Long limit) {
        return redisTemplate.opsForZSet().reverseRange(generateKey(boardId), offset, offset + limit -1)
                .stream().map(Long::valueOf).collect(Collectors.toList());
    }

    // 무한 스크롤 방식
    public List<Long> readAllInfiniteScroll(Long boardId, Long lastArticleId, Long limit) {
        // value로 정렬된 상태에서 조회
        return redisTemplate.opsForZSet().reverseRangeByLex(
                generateKey(boardId),
                // 시작점 셋팅.
                //      lastArticleId이 없으며 unbounded로 가장 위부터,
                //      있으면 Range.leftUnbounded 로 lastArticleId 다음부터 가져오도록
                lastArticleId == null ? Range.unbounded() : Range.leftUnbounded(Range.Bound.exclusive(toPaddingString(lastArticleId))),
                Limit.limit().count(limit.intValue())
        ).stream().map(Long::valueOf).toList();
    }

    // parameter 를 고정된 자리수의 문자열로 바꿈
    // 19자가 되도록 앞에 0을 채워줌
    private String toPaddingString(Long articleId) {
        return "%019d".formatted(articleId);
    }

    private String generateKey(Long boardId) {
        return KEY_FORMAT.formatted(boardId);
    }
}

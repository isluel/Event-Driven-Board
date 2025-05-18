package isluel.edb.articleread.client;

import isluel.edb.articleread.cache.OptimizedCacheable;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Slf4j
@Component
@RequiredArgsConstructor
public class ViewClient {
    @Value("${endpoints.board-view-service.url}")
    private String url;

    private RestClient restClient;

    @PostConstruct
    public void initRestClient() {
        restClient = RestClient.create(url);
    }

    // ArticleId 기준으로 cache 수행.. ttl은 cacheConfig에서 설정.
//    @Cacheable(key = "#articleId", value="articleViewCount")
    // OptimziedCache로 변경
    @OptimizedCacheable(type ="articleViewCount", ttpSeconds = 1)
    public Long count(Long articleId) {
        log.info("[ViewClient.count] articleId = {}", articleId);
        try {
            var response = restClient.get()
                    .uri("/v1/article-views/articles/{articleId}/count", articleId)
                    .retrieve()
                    .body(Long.class);
            return response;

        } catch (Exception e) {
            log.error("[ViewClient.count] articleId={}", articleId, e);
            return 0L;
        }
    }
}

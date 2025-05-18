package isluel.edb.articleread.client;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Slf4j
@Component
@RequiredArgsConstructor
public class LikeClient {
    @Value("${endpoints.board-like-service.url}")
    private String url;

    private RestClient restClient;

    @PostConstruct
    public void initRestClient() {
        restClient = RestClient.create(url);
    }

    public Long count(Long articleId) {
        try {
            var response = restClient.get()
                    .uri("/v1/article-likes/articles/{articleId}/count", articleId)
                    .retrieve()
                    .body(Long.class);
            return response;

        } catch (Exception e) {
            log.error("[LikeClient.count] articleId={}", articleId, e);
            return 0L;
        }
    }
}

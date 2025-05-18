package isluel.edb.articleread.client;

import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class ArticleClient {
    @Value("${endpoints.board-article-service.url}")
    private String url;

    private RestClient restClient;

    @PostConstruct
    public void initRestClient() {
        restClient = RestClient.create(url);
    }

    public Optional<ArticleResponse> read(Long articleId) {
        try {
            var response = restClient.get()
                    .uri("/v1/articles/{articleId}", articleId)
                    .retrieve()
                    .body(ArticleResponse.class);

            return Optional.ofNullable(response);

        } catch (Exception e) {
            log.error("[ArticleClient.read] articleId={}", articleId, e);
            return Optional.empty();
        }
    }

    public ArticlePageResponse readAll(Long boardId, Long page, Long pagSize) {
        try {
            return restClient.get()
                    .uri("/v1/articles?boardId=%s&page=%s&pageSize=%s".formatted(boardId, page, page))
                    .retrieve()
                    .body(ArticlePageResponse.class);
        } catch (Exception e) {
            log.error("[ArticleClient.readAll] boardId={}, page = {}, pageSize={}", boardId, page, pagSize, e);
            return ArticlePageResponse.EMPTY;
        }
    }

    // 무한 스크롤 방식
    public List<ArticleResponse> readAllInfiniteScroll(Long boardId, Long lastArticleId, Long pagSize) {
        try {
            return restClient.get()
                    .uri(
                            lastArticleId != null ?
                                "/v1/articles/infinite-scroll?boardId=%s&lastArticleId=%s&pageSize=%s".formatted(boardId, lastArticleId, pagSize)
                                    : "/v1/articles/infinite-scroll?boardId=%s&pageSize=%s".formatted(boardId, pagSize)
                            )
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<ArticleResponse>>() {});

        } catch (Exception e) {
            log.error("[ArticleClient.readAllInfiniteScroll] boardId={}, lastArticleId={}, pageSize={}", boardId, lastArticleId, pagSize, e);
            return List.of();
        }
    }

    // 게시글 수 반환
    public long count(Long boardId) {
        try {
            return restClient.get()
                    .uri("/v1/articles/boards/{boardId}/count", boardId)
                    .retrieve()
                    .body(Long.class);
        } catch (Exception e){
            log.error("[ArticleClient.count] boardId={}", boardId, e);
            return 0;
        }
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ArticlePageResponse {
        private List<ArticleResponse> articles;
        private Long articleCount;

        // 호출 에러시 반환 데이터
        public static ArticlePageResponse EMPTY = new ArticlePageResponse(List.of(), 0L);
    }

    // 서버에서 가져온 응답 객체
    @Getter
    public static class ArticleResponse {
        private Long articleId;
        private String title;
        private String content;
        private Long boardId;
        private Long writerId;
        private LocalDateTime createdAt;
        private LocalDateTime modifiedAt;
    }
}

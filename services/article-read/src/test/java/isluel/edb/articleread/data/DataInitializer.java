package isluel.edb.articleread.data;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

import java.util.random.RandomGenerator;

public class DataInitializer {
    RestClient articleServiceClient = RestClient.create("http://localhost:9000");
    RestClient commentServiceClient = RestClient.create("http://localhost:9001");
    RestClient likeServiceClient = RestClient.create("http://localhost:9002");
    RestClient viewServiceClient = RestClient.create("http://localhost:9003");
    RestClient hateServiceClient = RestClient.create("http://localhost:9006");

    @Test
    void initialize() {
        for (int i = 0; i < 5; i++) {
            var articleId = createArticle();
            System.out.println(articleId);
            var commentCount = RandomGenerator.getDefault().nextLong(10);
            var likeCount = RandomGenerator.getDefault().nextLong(10);
            var hateCount = RandomGenerator.getDefault().nextLong(30);
            var viewCount = RandomGenerator.getDefault().nextLong(200);

            createComment(articleId, commentCount);
            createLike(articleId, likeCount);
            createHate(articleId, hateCount);
            createView(articleId, viewCount);

        }
    }

    Long createArticle() {
        return articleServiceClient.post()
                .uri("/v1/articles")
                .body(new ArticleCreateRequest("title", "content", 1L, 1L))
                .retrieve()
                .body(ArticleResponse.class)
                .getArticleId();
    }

    @Getter
    @AllArgsConstructor
    static class ArticleCreateRequest{
        private String title;
        private String content;
        private Long writerId;
        private Long boardId;
    }

    @Getter
    static class ArticleResponse {
        private Long articleId;
    }

    void createComment(Long articleId, Long commentCount) {
        while(commentCount-- > 0) {
            commentServiceClient.post()
                    .uri("/v1/comments")
                    .body(new CommentCreateRequest(articleId, "content", 1L))
                    .retrieve();
        }
    }

    @Getter
    @AllArgsConstructor
    static class CommentCreateRequest {
        private Long articleId;
        private String content;
        private Long writerId;
    }

    void createLike(Long articleId, Long likeCount) {
        while(likeCount-- > 0) {
            likeServiceClient.post()
                    .uri("/v1/article-likes/articles/{articleId}/users/{userId}/like", articleId, likeCount)
                    .retrieve();
        }
    }

    void createHate(Long articleId, Long hateCount) {
        while(hateCount-- > 0) {
            hateServiceClient.post()
                    .uri("/v1/article-hates/articles/{articleId}/users/{userId}/hate", articleId, hateCount)
                    .retrieve();
        }
    }

    void createView(Long articleId, Long viewCount) {
        while(viewCount-- > 0) {
            viewServiceClient.post()
                    .uri("/v1/article-views/articles/{articleId}/users/{userId}", articleId, viewCount)
                    .retrieve();
        }
    }
}

package isluel.edb.article.api;

import isluel.edb.article.serivce.response.ArticlePageResponse;
import isluel.edb.article.serivce.response.ArticleResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.client.RestClient;

import java.util.List;

public class ArticleApiTest {

    RestClient restClient = RestClient.create("http://localhost:9000");

    @Test
    void createTest() {
        var response = create(new ArticleCreateRequest(
                "hi", "ny content", 1L, 1L
        ));
        System.out.printf("response: %s\n", response);
    }

    ArticleResponse create(ArticleCreateRequest request) {
        return restClient.post()
                .uri("/v1/articles")
                .body(request)
                .retrieve()
                .body(ArticleResponse.class);
    }

    @Getter
    @AllArgsConstructor
    static class ArticleCreateRequest {
        private String title;
        private String content;
        private Long writerId;
        private Long boardId;
    }

    @Test
    void readTest() {
        var response = read(178880710952722432L);
        System.out.println("response: " + response);
    }

    ArticleResponse read(Long articleId) {
        return restClient.get()
                .uri("/v1/articles/{articleId}", articleId)
                .retrieve()
                .body(ArticleResponse.class);
    }

    @Test
    void updateTest() {
        update(178880710952722432L);
        var response = read(178880710952722432L);
        System.out.println("response: " + response);
    }

    void update(Long articleId) {
        restClient.put()
                .uri("/v1/articles/{articleId}", articleId)
                .body(new ArticleUpdateRequest("h1 2", "my content 2"))
                .retrieve();
    }

    @Test
    void deleteTest() {
        restClient.delete()
                .uri("/v1/articles/{articleId}", 1L)
                .retrieve();
    }

    @Test
    void readAllTest() {
        ArticlePageResponse response = restClient.get()
                .uri("/v1/articles?boardId=1&page=1&pageSize=3")
                .retrieve()
                .body(ArticlePageResponse.class);

        System.out.println("response Count: " + response.getArticleCount());
        for (var article : response.getArticles()) {
            System.out.println("article Id: " + article.getArticleId());
        }
    }

    @Test
    void readAllInfiteScrollTest() {
        List<ArticleResponse> articles = restClient.get()
                .uri("/v1/articles/infinite-scroll?boardId=1&pageSize=5")
                .retrieve()
                .body(new ParameterizedTypeReference<List <ArticleResponse>>() {});

        System.out.println("first page");
        for (var article: articles) {
            System.out.println("articles: " + article);
        }

        var lastArticleId = articles.getLast().getArticleId();
        List<ArticleResponse> articles2 = restClient.get()
                .uri("/v1/articles/infinite-scroll?boardId=1&pageSize=5&lastArticleId=" + lastArticleId)
                .retrieve()
                .body(new ParameterizedTypeReference<List <ArticleResponse>>() {});
        for (var article: articles2) {
            System.out.println("articles: " + article);
        }
    }

    @Getter
    @AllArgsConstructor
    static class ArticleUpdateRequest {
        private String title;
        private String content;
    }

    @Test
    void countTest() {
        var article = create(new ArticleCreateRequest("hi", "ny content", 1L, 2L));

        var count = restClient.get()
                .uri("/v1/articles/boards/{boardId}/count", article.getBoardId())
                .retrieve()
                .body(Long.class);

        System.out.println("count: " + count);

        restClient.delete()
                .uri("/v1/articles/{articleId}", article.getArticleId())
                .retrieve();

        var count2 = restClient.get()
                .uri("/v1/articles/boards/{boardId}/count", article.getBoardId())
                .retrieve()
                .body(Long.class);

        System.out.println("count: " + count2);
    }

}

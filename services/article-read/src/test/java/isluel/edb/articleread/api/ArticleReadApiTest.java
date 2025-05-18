package isluel.edb.articleread.api;

import isluel.edb.articleread.service.response.ArticleReadPageResponse;
import isluel.edb.articleread.service.response.ArticleReadResponse;
import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.client.RestClient;

import java.util.List;

public class ArticleReadApiTest {
    RestClient articleReadRestClient = RestClient.create("http://localhost:9005");

    @Test
    void readTest() {
        var result = articleReadRestClient.get()
                .uri("/v1/articles/{articleId}", 181729836341899264L)
                .retrieve()
                .body(ArticleReadResponse.class);

        System.out.println("response = " + result);
    }

    // Article 원본 데이터 조회
    RestClient articleRestClient = RestClient.create("http://localhost:9000");

    @Test
    void readAllTest() {
        var response1 = articleReadRestClient.get()
                .uri("/v1/articles?boardId=%s&page=%s&pageSize=%s".formatted(1L, 1L, 5))
                .retrieve()
                .body(ArticleReadPageResponse.class);

        System.out.println("response count = " + response1.getArticleCount());
        for (var article : response1.getArticles()) {
            System.out.println("article id = " + article.getArticleId());
        }

        var responseOrigin = articleRestClient.get()
                .uri("/v1/articles?boardId=%s&page=%s&pageSize=%s".formatted(1L, 1L, 5))
                .retrieve()
                .body(ArticleReadPageResponse.class);

        System.out.println("origin response count = " + responseOrigin.getArticleCount());
        for (var article : responseOrigin.getArticles()) {
            System.out.println("origin article id = " + article.getArticleId());
        }
    }

    @Test
    void readAllInfiniteScroll() {
        var response1 = articleReadRestClient.get()
                .uri("/v1/articles/infinite-scroll?boardId=%s&pageSize=%s".formatted(1L, 5))
                .retrieve()
                .body(new ParameterizedTypeReference<List<ArticleReadResponse>>() {});

        for (var res : response1) {
            System.out.println("response = " + res.getArticleId());
        }

        var origin = articleRestClient.get()
                .uri("/v1/articles/infinite-scroll?boardId=%s&pageSize=%s".formatted(1L, 5))
                .retrieve()
                .body(new ParameterizedTypeReference<List<ArticleReadResponse>>() {});

        for (var res : origin) {
            System.out.println("origin response = " + res.getArticleId());
        }
    }
}

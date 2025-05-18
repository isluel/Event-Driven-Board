package isluel.edb.hotarticle.api;

import isluel.edb.hotarticle.service.response.HotArticleResponse;
import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.client.RestClient;

import java.util.List;

public class HotArticleApiTest {
    RestClient restClient = RestClient.create("http://localhost:9004");

    @Test
    void readAllTest() {
        var responses = restClient.get()
                .uri("/v1/hot-articles/articles/date/{dateString}", "20250518")
                .retrieve()
                .body(new ParameterizedTypeReference<List<HotArticleResponse>>() {
                });

        for (HotArticleResponse hotArticleResponse : responses) {
            System.out.println("response = " + hotArticleResponse);
        }
    }
}

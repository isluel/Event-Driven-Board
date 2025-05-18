package isluel.edb.hate.api;

import isluel.edb.hate.service.response.ArticleHateResponse;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

class ArticleHateApiTest {

    RestClient restClient = RestClient.create("http://localhost:9006");

    @Test
    void hateAndUnhateTes() {
        Long articleId = 9999L;

        hate(articleId, 1L);
        hate(articleId, 2L);
        hate(articleId, 3L);

        var response1 = read(articleId, 1L);
        var response2 = read(articleId, 2L);
        var response3 = read(articleId, 3L);

        System.out.println(response1);
        System.out.println(response2);
        System.out.println(response3);

        unhate(articleId, 1L);
        unhate(articleId, 2L);
        unhate(articleId, 3L);

        System.out.println();
    }

    void hate(Long articleId, Long userId) {
        restClient.post()
                .uri("/v1/article-hates/articles/{articleId}/users/{userId}/hate", articleId, userId)
                .retrieve();
    }

    void unhate(Long articleId, Long userId) {
        restClient.delete()
                .uri("/v1/article-hates/articles/{articleId}/users/{userId}/hate", articleId, userId)
                .retrieve();
    }

    ArticleHateResponse read(Long articleId, Long userId) {
        return restClient.get()
                .uri("v1/article-hates/articles/{articleId}/users/{userId}", articleId, userId)
                .retrieve()
                .body(ArticleHateResponse.class);
    }

    @Test
    void hatePerformanceTest() throws InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(100);
        hatePerformanceTest(executorService, 1111L, "pessimistic-lock-1");
    }

    void hatePerformanceTest(ExecutorService executorService, Long articleId, String lockType) throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(3000);
        System.out.println("lock type : " + lockType);

        // 데이터 없는 경우 추가
        hate(articleId, 1L, lockType);

        long start = System.nanoTime();
        for (int i =0; i < 3000; i++) {
            long userId = i + 2;
            executorService.submit(() -> {
                hate(articleId, userId, lockType);
                latch.countDown();
            });
        }

        latch.await();
        long end = System.nanoTime();

        System.out.println(" lock Type = " + lockType + " time = " + (end - start) / 1000000 + "ms");
        System.out.println(lockType + " end");

        Long count = restClient.get()
                .uri("/v1/article-hates/articles/{articleId}/count", articleId)
                .retrieve()
                .body(Long.class);

        System.out.println("count: " + count);

    }

    void hate(Long articleId, Long userId, String lockType) {
        restClient.post()
                .uri("/v1/article-hates/articles/{articleId}/users/{userId}/hate" + lockType, articleId, userId)
                .retrieve();
    }
}
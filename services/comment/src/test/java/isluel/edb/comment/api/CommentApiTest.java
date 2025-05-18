package isluel.edb.comment.api;

import isluel.edb.comment.service.response.CommentPageResponse;
import isluel.edb.comment.service.response.CommentResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.client.RestClient;

import java.util.List;

public class CommentApiTest {

    RestClient restClient = RestClient.create("http://localhost:9001");

    @Test
    void create() {
        var response1 = create(new CommentCreateRequest(1L, "my Comment 1", null, 1L));
        var response2 = create(new CommentCreateRequest(1L, "my Comment 2", response1.getPath(), 1L));
        var response3 = create(new CommentCreateRequest(1L, "my Comment 3", response2.getPath(), 1L));

        System.out.println("response 1 = " + response1.getCommentId());
        System.out.println("\tresponse 2 = " + response2.getCommentId());
        System.out.println("\t\tresponse 3 = " + response3.getCommentId());
    }

    CommentResponse create(CommentCreateRequest request) {
        return restClient.post()
                .uri("/v1/comments")
                .body(request)
                .retrieve()
                .body(CommentResponse.class);
    }


    @Getter
    @AllArgsConstructor
    public class CommentCreateRequest {
        private Long articleId;
        private String content;;
        private String parentPath;
        private Long writerId;
    }

    @Test
    void read() {
        var response = restClient.get()
                .uri("/v1/comments/{commentId}", 182389297728786432L)
                .retrieve()
                .body(CommentResponse.class);

        System.out.println("response = " + response.getCommentId());
    }

    @Test
    void delete() {
        var response = restClient.delete()
                .uri("/v1/comments/{commentId}", 182389297728786432L)
                .retrieve();
    }

    @Test
    void readAll() {
        var response = restClient.get()
                .uri("/v1/comments?articleId=1&page=1&pageSize=10")
                .retrieve()
                .body(CommentPageResponse.class);

        System.out.println("response = " + response.getCommentCount());
        for (var comment : response.getComments()) {
            System.out.println(comment);
        }
    }

    @Test
    void readAllInfiniteScroll() {
        var responseList1 = restClient.get()
                .uri("/v1/comments/infinite-scroll?articleId=1&pageSize=5")
                .retrieve()
                .body(new ParameterizedTypeReference<List<CommentResponse>>() {
                });

        System.out.println("firstPage");
        for (var response : responseList1) {
            System.out.println("response = " + response);
        }

        var responseList2 = restClient.get()
                .uri("/v1/comments/infinite-scroll?articleId=2&pageSize=5&lastPath=" + responseList1.getLast().getPath())
                .retrieve()
                .body(new ParameterizedTypeReference<List<CommentResponse>>() {});

        System.out.println("secondPage");
        for (var response : responseList2) {
            System.out.println("response = " + response);
        }

    }

    @Test
    void countTest() {
        var comment = create(new CommentCreateRequest(2L, "my Comment 1", null, 1L));

        var count = restClient.get()
                .uri("/v1/comments/articles/{articleId}/count", comment.getArticleId())
                .retrieve()
                .body(Long.class);

        System.out.println("count = " + count);

        restClient.delete()
                .uri("/v1/comments/{commentId}", comment.getCommentId())
                .retrieve();

        var count2 = restClient.get()
                .uri("/v1/comments/articles/{articleId}/count", comment.getArticleId())
                .retrieve()
                .body(Long.class);
        System.out.println("count2 = " + count2);
    }
}

package isluel.edb.hate.service.response;

import isluel.edb.hate.entity.ArticleHate;
import lombok.Getter;
import lombok.ToString;

import java.time.LocalDateTime;

@Getter
@ToString
public class ArticleHateResponse {
    private Long articleHateId;
    private Long articleId;
    private Long userId;
    private LocalDateTime createdAt;

    public static ArticleHateResponse from(ArticleHate articleLike) {
        ArticleHateResponse response = new ArticleHateResponse();
        response.articleHateId = articleLike.getArticleHateId();
        response.articleId = articleLike.getArticleId();
        response.userId = articleLike.getUserId();
        response.createdAt = articleLike.getCreatedAt();
        return response;
    }
}

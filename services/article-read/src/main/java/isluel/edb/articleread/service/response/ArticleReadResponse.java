package isluel.edb.articleread.service.response;

import isluel.edb.articleread.repository.ArticleQueryModel;
import lombok.Getter;
import lombok.ToString;

import java.time.LocalDateTime;

@Getter
@ToString
public class ArticleReadResponse {
    private Long articleId;
    private String title;
    private String content;
    private Long writerId;
    private Long boardId;
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;
    private Long articleCommentCount;
    private Long articleLikeCount;
    private Long articleViewCount;

    public static ArticleReadResponse from(ArticleQueryModel articleQueryModel, Long viewCount) {
        ArticleReadResponse articleReadResponse = new ArticleReadResponse();
        articleReadResponse.articleId = articleQueryModel.getArticleId();
        articleReadResponse.title = articleQueryModel.getTitle();
        articleReadResponse.content = articleQueryModel.getContent();
        articleReadResponse.writerId = articleQueryModel.getWriterId();
        articleReadResponse.boardId = articleQueryModel.getBoardId();
        articleReadResponse.createdAt = articleQueryModel.getCreatedAt();
        articleReadResponse.modifiedAt = articleQueryModel.getModifiedAt();
        articleReadResponse.articleCommentCount = articleQueryModel.getArticleCommentCount();
        articleReadResponse.articleLikeCount = articleQueryModel.getArticleLikeCount();
        articleReadResponse.articleViewCount = viewCount;
        return articleReadResponse;
    }
}

package isluel.edb.articleread.repository;

import isluel.edb.articleread.client.ArticleClient;
import isluel.edb.common.event.payload.*;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class ArticleQueryModel {
    private Long articleId;
    private String title;
    private String content;
    private Long boardId;
    private Long writerId;
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;
    private Long articleCommentCount;
    private Long articleLikeCount;
    private Long articleHateCount;

    public static ArticleQueryModel create(ArticleCreatedEventPayload payload) {
        ArticleQueryModel model = new ArticleQueryModel();
        model.articleId = payload.getArticleId();
        model.title = payload.getTitle();
        model.content = payload.getContent();
        model.boardId = payload.getBoardId();
        model.writerId = payload.getWriterId();
        model.createdAt = payload.getCreatedAt();
        model.modifiedAt = payload.getModifiedAt();
        model.articleCommentCount = 0L;
        model.articleLikeCount = 0L;
        model.articleHateCount = 0L;
        return model;
    }

    public static ArticleQueryModel create(ArticleClient.ArticleResponse response, Long articleCommentCount, Long articleLikeCount, Long articleHateCount) {
        ArticleQueryModel model = new ArticleQueryModel();
        model.articleId = response.getArticleId();
        model.title = response.getTitle();
        model.content = response.getContent();
        model.boardId = response.getBoardId();
        model.writerId = response.getWriterId();
        model.createdAt = response.getCreatedAt();
        model.modifiedAt = response.getModifiedAt();
        model.articleCommentCount = articleCommentCount;
        model.articleLikeCount = articleLikeCount;
        model.articleHateCount = articleHateCount;
        return model;
    }

    // 게시글 수정, 삭제, 댓글 추가, 댓글 삭제 등 이벤트 처리
    public void updateBy(CommentCreatedEventPayload payload) {
        this.articleCommentCount = payload.getArticleCommentCount();
    }

    public void updateBy(CommentDeletedEventPayload payload) {
        this.articleCommentCount = payload.getArticleCommentCount();
    }

    public void updateBy(ArticleLikedEventPayload payload) {
        this.articleLikeCount = payload.getArticleLikeCount();
    }

    public void updateBy(ArticleHatedEventPayload payload) {
        this.articleHateCount = payload.getArticleHateCount();
    }

    public void updateBy(ArticleUnlikedEventPayload payload) {
        this.articleLikeCount = payload.getArticleLikeCount();
    }

    public void updateBy(ArticleUnhatedEventPayload payload) {
        this.articleLikeCount = payload.getArticleHateCount();
    }

    public void updateBy(ArticleUpdatedPayload payload) {
        this.title = payload.getTitle();
        this.content = payload.getContent();
        this.boardId = payload.getBoardId();
        this.writerId = payload.getWriterId();
        this.createdAt = payload.getCreatedAt();
        this.modifiedAt = payload.getModifiedAt();
    }
}

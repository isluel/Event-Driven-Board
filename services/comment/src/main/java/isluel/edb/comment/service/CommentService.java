package isluel.edb.comment.service;

import isluel.edb.comment.entity.ArticleCommentCount;
import isluel.edb.comment.entity.Comment;
import isluel.edb.comment.entity.CommentPath;
import isluel.edb.comment.repository.ArticleCommentRepository;
import isluel.edb.comment.repository.CommentRepository;
import isluel.edb.comment.service.request.CommentCreateRequest;
import isluel.edb.comment.service.response.CommentPageResponse;
import isluel.edb.comment.service.response.CommentResponse;
import isluel.edb.common.event.EventType;
import isluel.edb.common.event.payload.CommentCreatedEventPayload;
import isluel.edb.common.event.payload.CommentDeletedEventPayload;
import isluel.edb.common.outboxmessagingrelay.OutboxEventPublisher;
import isluel.edb.common.snowflake.Snowflake;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

import static java.util.function.Predicate.not;

@Service
@RequiredArgsConstructor
public class CommentService {
    private final CommentRepository commentRepository;
    private final Snowflake snowflake = new Snowflake();
    private final ArticleCommentRepository articleCommentRepository;
    private final OutboxEventPublisher outboxEventPublisher;

    @Transactional
    public CommentResponse create(CommentCreateRequest request) {
        // 상위 객체 찾기
        var parent = findParent(request);

        var parentCommendPath = parent == null ? CommentPath.create("") : parent.getCommentPath();
        var comment = commentRepository.save(
                Comment.create(
                        snowflake.nextId(),
                        request.getContent(),
                        request.getArticleId(),
                        request.getWriterId(),
                        parentCommendPath.createChildCommentPath(
                                commentRepository.findDescendantTopPath(request.getArticleId(), parentCommendPath.getPath())
                                        .orElse(null)
                        )
                )
        );

        var result = articleCommentRepository.increase(comment.getArticleId());
        if (result == 0) {
            articleCommentRepository.save(
                    ArticleCommentCount.init(comment.getArticleId(), 1L)
            );
        }

        outboxEventPublisher.publish(EventType.COMMENT_CREATED,
                CommentCreatedEventPayload.builder()
                        .commentId(comment.getCommentId())
                        .content(comment.getContent())
                        .articleId(comment.getArticleId())
                        .writerId(comment.getWriterId())
                        .deleted(comment.getDeleted())
                        .createdAt(comment.getCreatedAt())
                        .articleCommentCount(count(comment.getArticleId()))
                        .build(),
                comment.getArticleId()
                );

        return CommentResponse.from(comment);
    }

    private Comment findParent(CommentCreateRequest request) {
        String parentPath = request.getParentPath();
        if (parentPath == null)
            return null;

        return commentRepository.findByPath(parentPath)
                .filter(not(Comment::getDeleted))
                .orElseThrow();
    }

    public CommentResponse read(Long commentId) {
        return CommentResponse.from(
                commentRepository.findById(commentId).orElseThrow()
        );
    }

    @Transactional
    public void delete(Long commentId) {
        commentRepository.findById(commentId)
                .filter(not(Comment::getDeleted))
                .ifPresent(comment -> {
                    if(hasChildren(comment))
                        comment.delete();
                    else
                        delete(comment);
                });

    }

    private boolean hasChildren(Comment comment) {
        return commentRepository.findDescendantTopPath(
                comment.getArticleId(),
                comment.getCommentPath().getPath()
        ).isPresent();
    }

    private void delete(Comment comment) {
        commentRepository.delete(comment);
        if(!comment.isRoot()) {
            commentRepository.findByPath(comment.getCommentPath().getParentPath())
                    .filter(Comment::getDeleted)
                    .filter(not(this::hasChildren))
                    .ifPresent(this::delete);
        }

        articleCommentRepository.decrease(comment.getArticleId());

        outboxEventPublisher.publish(EventType.COMMENT_DELETED,
                CommentDeletedEventPayload.builder()
                        .commentId(comment.getCommentId())
                        .content(comment.getContent())
                        .articleId(comment.getArticleId())
                        .writerId(comment.getWriterId())
                        .deleted(comment.getDeleted())
                        .createdAt(comment.getCreatedAt())
                        .articleCommentCount(count(comment.getArticleId()))
                        .build(),
                comment.getArticleId()
        );
    }

    public CommentPageResponse readAll(Long articleId, Long page, Long pageSize) {
        return CommentPageResponse.of(
                commentRepository.findAll(articleId, (page - 1) * pageSize, pageSize)
                        .stream()
                        .map(CommentResponse::from)
                        .toList(),
                commentRepository.count(articleId, PageLimitCalculator.calculateArticleLimit(page, pageSize, 10L))
                // 대체 가능
//                count(articleId)
        );
    }

    public List<CommentResponse> readAll(Long articleId, String lastPath, Long pageSize) {
        var comments = lastPath == null ?
                commentRepository.findAllInfiniteScroll(articleId, pageSize) :
                commentRepository.findAllInfiniteScroll(articleId, lastPath, pageSize);

        return comments.stream()
                .map(CommentResponse::from)
                .toList();
    }

    public Long count(Long articleId) {
        return articleCommentRepository.findById(articleId)
                .map(ArticleCommentCount::getCommentCount)
                .orElse(0L);
    }
}

package isluel.edb.comment.service;

import isluel.edb.comment.entity.Comment;
import isluel.edb.comment.entity.CommentPath;
import isluel.edb.comment.repository.ArticleCommentRepository;
import isluel.edb.comment.repository.CommentRepository;
import isluel.edb.common.outboxmessagingrelay.OutboxEventPublisher;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    @InjectMocks
    CommentService commentService;
    @Mock
    CommentRepository commentRepository;
    @Mock
    ArticleCommentRepository articleCommentRepository;
    @Mock
    OutboxEventPublisher outboxEventPublisher;

    @DisplayName("삭제할 댓글이 자식이 있으면, 삭제 표시만 한다.")
    @Test
    void deleteShouldMarkDeletedIfHasChidren() {
        // given
        Long articleId = 1L;
        Long commentId = 2L;
        var comment = mock(Comment.class);
        BDDMockito.given(comment.getArticleId()).willReturn(articleId);
        BDDMockito.given(comment.getCommentPath()).willReturn(CommentPath.create("00000"));
        given(commentRepository.findById(commentId))
                .willReturn(Optional.of(comment));
        given(commentRepository.findDescendantTopPath(articleId, "00000"))
                .willReturn(Optional.of(""));

        // when
        commentService.delete(commentId);

        // then
        verify(comment).delete();
    }

    @DisplayName("하위 댓글이 삭제되고, 삭제되지 않은 부모면, 하위 댓글만 삭제한다.")
    @Test
    void deleteShouldMarkDeletedIfHasChidrenOnlyIfDeletedParent() {
        // given
        Long articleId = 1L;
        Long commentId = 2L;
        Long parentCommentId = 3L;

        var comment = mock(Comment.class);
        BDDMockito.given(comment.getArticleId()).willReturn(articleId);
        BDDMockito.given(comment.getCommentPath()).willReturn(CommentPath.create("0000000000"));
        BDDMockito.given(comment.isRoot()).willReturn(false);

        var parentComment = mock(Comment.class);
        BDDMockito.given(parentComment.getDeleted()).willReturn(false);

        BDDMockito.given(commentRepository.findById(commentId)).willReturn(Optional.of(comment));
        BDDMockito.given(commentRepository.findByPath("00000"))
                .willReturn(Optional.of(parentComment));
        BDDMockito.given(commentRepository.findDescendantTopPath(articleId, "0000000000"))
                .willReturn(Optional.empty());

        // when
        commentService.delete(commentId);

        // then
        verify(commentRepository).delete(comment);
        verify(commentRepository, never()).deleteById(parentCommentId);
    }

    @DisplayName("하위 댓글이 삭제되고, 삭제된 부모면, 재귀적으로 부모까지 모두 삭제한다.")
    @Test
    void deleteShouldDeleteAllRecursivelyIfDeletedParent() {
        // given
        Long articleId = 1L;
        Long commentId = 2L;
        Long parentCommentId = 1L;

        var comment = createComment(articleId, commentId, "0000000000");
        given(comment.isRoot()).willReturn(false);

        var parentComment = createComment(articleId, parentCommentId, "00000");
        given(parentComment.isRoot()).willReturn(true);
        given(parentComment.getDeleted()).willReturn(true);

        given(commentRepository.findById(commentId)).willReturn(Optional.of(comment));
        given(commentRepository.findByPath("00000")).willReturn(Optional.of(parentComment));
        given(commentRepository.findDescendantTopPath(articleId, "0000000000")).willReturn(Optional.empty());
        given(commentRepository.findDescendantTopPath(articleId, "00000")).willReturn(Optional.empty());

        // when
        commentService.delete(commentId);

        // then
        verify(commentRepository).delete(comment);
        verify(commentRepository).delete(parentComment);
    }

    private Comment createComment(Long articleId, Long commentId, String path) {
        var comment = mock(Comment.class);
        BDDMockito.given(comment.getArticleId()).willReturn(articleId);
        BDDMockito.given(comment.getCommentId()).willReturn(commentId);
        BDDMockito.given(comment.getCommentPath()).willReturn(CommentPath.create(path));
        return comment;
    }
}
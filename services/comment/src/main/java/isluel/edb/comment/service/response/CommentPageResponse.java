package isluel.edb.comment.service.response;

import lombok.Getter;

import java.util.List;

@Getter
public class CommentPageResponse {
    private List<CommentResponse> comments;
    private Long commentCount;

    public static CommentPageResponse of(List<CommentResponse> commentResponse, Long commentCount) {
        CommentPageResponse commentPageResponse = new CommentPageResponse();
        commentPageResponse.comments = commentResponse;
        commentPageResponse.commentCount = commentCount;
        return commentPageResponse;
    }
}

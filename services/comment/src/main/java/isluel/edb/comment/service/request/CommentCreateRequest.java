package isluel.edb.comment.service.request;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class CommentCreateRequest {
    private Long articleId;
    private String content;;
    private String parentPath;
    private Long writerId;
}

package isluel.edb.common.event.payload;

import isluel.edb.common.event.EventPayload;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ArticleViewEventPayload implements EventPayload {
    private Long articleId;
    private long articleViewCount;
}

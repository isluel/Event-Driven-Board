package isluel.edb.common.event.payload;

import isluel.edb.common.event.EventPayload;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ArticleHatedEventPayload implements EventPayload {
    private Long articleHateId;
    private Long articleId;
    private Long userId;
    private LocalDateTime createdAt;
    private Long articleHateCount;
}

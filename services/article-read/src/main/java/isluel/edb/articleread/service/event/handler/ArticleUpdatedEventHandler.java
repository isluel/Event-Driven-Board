package isluel.edb.articleread.service.event.handler;

import isluel.edb.articleread.repository.ArticleQueryModelRepository;
import isluel.edb.common.event.Event;
import isluel.edb.common.event.EventType;
import isluel.edb.common.event.payload.ArticleUpdatedPayload;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ArticleUpdatedEventHandler implements EventHandler<ArticleUpdatedPayload> {
    private final ArticleQueryModelRepository articleQueryModelRepository;

    @Override
    public void handle(Event<ArticleUpdatedPayload> event) {
        // 기존 데이터 읽어오기
        articleQueryModelRepository.read(event.getPayload().getArticleId())
                .ifPresent(articleQueryModel -> {
                    articleQueryModel.updateBy(event.getPayload());
                    articleQueryModelRepository.update(articleQueryModel);
                });
    }

    @Override
    public boolean supports(Event<ArticleUpdatedPayload> event) {
        return EventType.ARTICLE_UPDATED.equals(event.getType());
    }
}

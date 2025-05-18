package isluel.edb.articleread.service.event.handler;

import isluel.edb.articleread.repository.ArticleQueryModelRepository;
import isluel.edb.common.event.Event;
import isluel.edb.common.event.EventType;
import isluel.edb.common.event.payload.ArticleUnhatedEventPayload;
import isluel.edb.common.event.payload.ArticleUnlikedEventPayload;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ArticleUnhateEventHandler implements EventHandler<ArticleUnhatedEventPayload> {
    private final ArticleQueryModelRepository articleQueryModelRepository;

    @Override
    public void handle(Event<ArticleUnhatedEventPayload> event) {
        // 기존 데이터 읽어오기
        articleQueryModelRepository.read(event.getPayload().getArticleId())
                .ifPresent(articleQueryModel -> {
                    articleQueryModel.updateBy(event.getPayload());
                    articleQueryModelRepository.update(articleQueryModel);
                });
    }

    @Override
    public boolean supports(Event<ArticleUnhatedEventPayload> event) {
        return EventType.ARTICLE_UNHATE.equals(event.getType());
    }
}

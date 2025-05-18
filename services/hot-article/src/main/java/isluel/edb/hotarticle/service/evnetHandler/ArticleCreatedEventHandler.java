package isluel.edb.hotarticle.service.evnetHandler;

import isluel.edb.common.event.Event;
import isluel.edb.common.event.EventType;
import isluel.edb.common.event.payload.ArticleCreatedEventPayload;
import isluel.edb.hotarticle.repository.ArticleCreatedTimeRepository;
import isluel.edb.hotarticle.utils.TimeCalculatorUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ArticleCreatedEventHandler implements EventHandler<ArticleCreatedEventPayload> {
    private final ArticleCreatedTimeRepository articleCreatedTimeRepository;

    // Event로 받은 데이터를 오늘 자정까지만 저장하도록 한다.
    @Override
    public void handle(Event<ArticleCreatedEventPayload> event) {
        var payload = event.getPayload();
        articleCreatedTimeRepository.createOrUpdate(payload.getArticleId(), payload.getCreatedAt()
                , TimeCalculatorUtils.calculationDurationToMidnight());
    }

    @Override
    public boolean supports(Event<ArticleCreatedEventPayload> eventType) {
        return EventType.ARTICLE_CREATED == eventType.getType();
    }

    @Override
    public Long findArticleId(Event<ArticleCreatedEventPayload> event) {
        return event.getPayload().getArticleId();
    }
}

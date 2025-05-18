package isluel.edb.hotarticle.service.evnetHandler;

import isluel.edb.common.event.Event;
import isluel.edb.common.event.EventType;
import isluel.edb.common.event.payload.ArticleViewEventPayload;
import isluel.edb.hotarticle.repository.ArticleViewCountRepository;
import isluel.edb.hotarticle.utils.TimeCalculatorUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ArticleViewedEventHandler implements EventHandler<ArticleViewEventPayload> {
    private final ArticleViewCountRepository articleViewCountRepository;

    // Event로 받은 데이터의 정보를 Redis 에서 저장한다..
    @Override
    public void handle(Event<ArticleViewEventPayload> event) {
        var payload = event.getPayload();
        articleViewCountRepository.createOrUpdate(payload.getArticleId(), payload.getArticleViewCount()
                , TimeCalculatorUtils.calculationDurationToMidnight());

    }

    @Override
    public boolean supports(Event<ArticleViewEventPayload> eventType) {
        return EventType.ARTICLE_VIEWED == eventType.getType();
    }

    @Override
    public Long findArticleId(Event<ArticleViewEventPayload> event) {
        return event.getPayload().getArticleId();
    }
}
 
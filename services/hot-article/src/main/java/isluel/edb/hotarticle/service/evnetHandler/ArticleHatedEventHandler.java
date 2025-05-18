package isluel.edb.hotarticle.service.evnetHandler;

import isluel.edb.common.event.Event;
import isluel.edb.common.event.EventType;
import isluel.edb.common.event.payload.ArticleHatedEventPayload;
import isluel.edb.common.event.payload.ArticleLikedEventPayload;
import isluel.edb.hotarticle.repository.ArticleHateCountRepository;
import isluel.edb.hotarticle.repository.ArticleLikeCountRepository;
import isluel.edb.hotarticle.utils.TimeCalculatorUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ArticleHatedEventHandler implements EventHandler<ArticleHatedEventPayload> {
    private final ArticleHateCountRepository articleHateCountRepository;

    // Event로 받은 데이터의 정보를 Redis 에서 삭제한다.
    @Override
    public void handle(Event<ArticleHatedEventPayload> event) {
        var payload = event.getPayload();
        articleHateCountRepository.createOrUpdate(payload.getArticleId(), payload.getArticleHateCount()
                , TimeCalculatorUtils.calculationDurationToMidnight());

    }

    @Override
    public boolean supports(Event<ArticleHatedEventPayload> eventType) {
        return EventType.ARTICLE_HATE == eventType.getType();
    }

    @Override
    public Long findArticleId(Event<ArticleHatedEventPayload> event) {
        return event.getPayload().getArticleId();
    }
}
 
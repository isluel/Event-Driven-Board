package isluel.edb.hotarticle.service.evnetHandler;

import isluel.edb.common.event.Event;
import isluel.edb.common.event.EventType;
import isluel.edb.common.event.payload.ArticleUnlikedEventPayload;
import isluel.edb.hotarticle.repository.ArticleLikeCountRepository;
import isluel.edb.hotarticle.utils.TimeCalculatorUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ArticleUnLikedEventHandler implements EventHandler<ArticleUnlikedEventPayload> {
    private final ArticleLikeCountRepository articleLikeCountRepository;

    // Event로 받은 데이터의 정보를 Redis 에서 삭제한다.
    @Override
    public void handle(Event<ArticleUnlikedEventPayload> event) {
        var payload = event.getPayload();
        articleLikeCountRepository.createOrUpdate(payload.getArticleId(), payload.getArticleLikeCount()
                , TimeCalculatorUtils.calculationDurationToMidnight());

    }

    @Override
    public boolean supports(Event<ArticleUnlikedEventPayload> eventType) {
        return EventType.ARTICLE_UNLIKED == eventType.getType();
    }

    @Override
    public Long findArticleId(Event<ArticleUnlikedEventPayload> event) {
        return event.getPayload().getArticleId();
    }
}
 
package isluel.edb.hotarticle.service.evnetHandler;

import isluel.edb.common.event.Event;
import isluel.edb.common.event.EventType;
import isluel.edb.common.event.payload.ArticleDeletedEventPayload;
import isluel.edb.hotarticle.repository.ArticleCreatedTimeRepository;
import isluel.edb.hotarticle.repository.HotArticleListRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ArticleDeletedEventHandler implements EventHandler<ArticleDeletedEventPayload> {
    private final ArticleCreatedTimeRepository articleCreatedTimeRepository;
    private final HotArticleListRepository hotArticleListRepository;

    // Event로 받은 데이터의 정보를 Redis 에서 삭제한다.
    @Override
    public void handle(Event<ArticleDeletedEventPayload> event) {
        var payload = event.getPayload();
        articleCreatedTimeRepository.remove(payload.getArticleId());
        hotArticleListRepository.remove(payload.getArticleId(), payload.getCreatedAt());
    }

    @Override
    public boolean supports(Event<ArticleDeletedEventPayload> eventType) {
        return EventType.ARTICLE_DELETED == eventType.getType();
    }

    @Override
    public Long findArticleId(Event<ArticleDeletedEventPayload> event) {
        return event.getPayload().getArticleId();
    }
}

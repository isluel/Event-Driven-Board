package isluel.edb.hotarticle.service;

import isluel.edb.common.event.Event;
import isluel.edb.common.event.EventPayload;
import isluel.edb.common.event.EventType;
import isluel.edb.hotarticle.client.ArticleClient;
import isluel.edb.hotarticle.repository.HotArticleListRepository;
import isluel.edb.hotarticle.service.evnetHandler.EventHandler;
import isluel.edb.hotarticle.service.response.HotArticleResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class HotArticleService {
    // 원본 데이터 조회시 사용
    private final ArticleClient articleClient;
    private final List<EventHandler> eventHandlers;
    private final HotArticleScoreUpdater hotArticleScoreUpdater;
    private final HotArticleListRepository hotArticleListRepository;

    // 이벤트를 통해서 이벤트에 해당되는 게시글의 점수를 계산하여
    // hotArticleList Repository 에 인기글을 저장
    public void handleEvent(Event<EventPayload> event) {
        // 이벤트가 어떤 Handler를 통해 처리되야하는지 찾는다.
        EventHandler<EventPayload> eventHandler = findEventHandler(event);
        if (eventHandler == null) {
            return;
        }

        // 이벤트가 게시글 생성 또는 삭제인지 확인하여
        // 게시글 생성 또는 삭제 인 경우는 점수 업데이트가 필요없다.
        if (isArticleCreatedOrDeleted(event)) {
            eventHandler.handle(event);
        } else {
            hotArticleScoreUpdater.update(event, eventHandler);
        }

    }

    private EventHandler<EventPayload> findEventHandler(Event<EventPayload> event) {
        return eventHandlers.stream()
                .filter(handler -> handler.supports(event))
                .findAny()
                .orElse(null);
    }

    // 이벤트가 게시글 생성 또는 삭제인지 확인
    private boolean isArticleCreatedOrDeleted(Event<EventPayload> event) {
        return EventType.ARTICLE_CREATED == event.getType() || EventType.ARTICLE_DELETED == event.getType();
    }

    // 데이터 조회하는 함수
    // dateString : yyyyMMdd
    public List<HotArticleResponse> readAll(String dateString) {
        // Redis의 인기글 정보를 가져온뒤
        // 해당 데이터의 상세 정보는 실제 원본데이터를 조회하도록 한다.
        return hotArticleListRepository.readAll(dateString)
                .stream().map(articleClient::read)
                .filter(Objects::nonNull)
                .map(HotArticleResponse::from)
                .toList();
    }
}

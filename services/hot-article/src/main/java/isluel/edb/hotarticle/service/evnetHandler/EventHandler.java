package isluel.edb.hotarticle.service.evnetHandler;


import isluel.edb.common.event.Event;
import isluel.edb.common.event.EventPayload;

// 이벤트를 kafka로 받았을때 처리해주는 handler 인터페이스
public interface EventHandler<T extends EventPayload> {
    // 이벤트를 처리하는 함수
    void handle(Event<T> event);
    // 이벤트가 해당 Class를 지원하는지 여부...
    // 이벤트 종류는 Event 안에 EventType에 따라 다르다.
    // handler class를 EventType 만큼 존재해야한다.
    boolean supports(Event<T> eventType);
    // 해당 이벤트가 어떤 Article ID에 해당되는 Event인지 찾아주는 함수
    Long findArticleId(Event<T> event);
}

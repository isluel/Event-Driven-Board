package isluel.edb.hotarticle.service;

import isluel.edb.common.event.Event;
import isluel.edb.common.event.EventType;
import isluel.edb.hotarticle.service.evnetHandler.EventHandler;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;
import java.util.stream.Stream;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;

@ExtendWith(SpringExtension.class)
class HotArticleServiceTest {

    @InjectMocks
    HotArticleService hotArticleService;
    @Mock
    List<EventHandler> eventHandlers;
    @Mock
    HotArticleScoreUpdater hotArticleScoreUpdater;

    @DisplayName("이벤트 핸들러가 없는 이벤트는 처리하지 않는다.")
    @Test
    void handleEventIfEventHandlerNotFound() {
        // given
        Event event = mock(Event.class);
        EventHandler eventHandler = mock(EventHandler.class);
        BDDMockito.given(eventHandler.supports(event)).willReturn(false);
        BDDMockito.given(eventHandlers.stream()).willReturn(Stream.of(eventHandler));

        // when
        hotArticleService.handleEvent(event);

        // then
        BDDMockito.verify(eventHandler, never()).handle(event);
        BDDMockito.verify(hotArticleScoreUpdater, never()).update(event, eventHandler);
    }

    @DisplayName("Article 생성 이벤트에 대해 처리는 하나 score는 업데이트하지 않는다.")
    @Test
    void handleEventIfArticleCreatedEventTest() {
        // given
        Event event = mock(Event.class);
        BDDMockito.given(event.getType()).willReturn(EventType.ARTICLE_CREATED);

        EventHandler eventHandler = mock(EventHandler.class);
        BDDMockito.given(eventHandler.supports(event)).willReturn(true);
        BDDMockito.given(eventHandlers.stream()).willReturn(Stream.of(eventHandler));

        // when
        hotArticleService.handleEvent(event);

        // then
        BDDMockito.verify(eventHandler).handle(event);
        BDDMockito.verify(hotArticleScoreUpdater, never()).update(event, eventHandler);
    }

    @DisplayName("Article 삭제 이벤트에 대해 처리는 하나 score는 업데이트하지 않는다.")
    @Test
    void handleEventIfArticleDeletedEventTest() {
        // given
        Event event = mock(Event.class);
        BDDMockito.given(event.getType()).willReturn(EventType.ARTICLE_DELETED);

        EventHandler eventHandler = mock(EventHandler.class);
        BDDMockito.given(eventHandler.supports(event)).willReturn(true);
        BDDMockito.given(eventHandlers.stream()).willReturn(Stream.of(eventHandler));

        // when
        hotArticleService.handleEvent(event);

        // then
        BDDMockito.verify(eventHandler).handle(event);
        BDDMockito.verify(hotArticleScoreUpdater, never()).update(event, eventHandler);
    }

    @DisplayName("Article Count 변화 이벤트에 대해 이벤트 처리와 Score update를 모두 진행한다.")
    @Test
    void handleEventIfScoreUpdatableEventTest() {
        // given
        Event event = mock(Event.class);
        BDDMockito.given(event.getType()).willReturn(mock(EventType.class));

        EventHandler eventHandler = mock(EventHandler.class);
        BDDMockito.given(eventHandler.supports(event)).willReturn(true);
        BDDMockito.given(eventHandlers.stream()).willReturn(Stream.of(eventHandler));

        // when
        hotArticleService.handleEvent(event);

        // then
        BDDMockito.verify(eventHandler, never()).handle(event);
        BDDMockito.verify(hotArticleScoreUpdater).update(event, eventHandler);

    }
}
package isluel.edb.hotarticle.service;

import isluel.edb.common.event.Event;
import isluel.edb.hotarticle.repository.ArticleCreatedTimeRepository;
import isluel.edb.hotarticle.repository.HotArticleListRepository;
import isluel.edb.hotarticle.service.evnetHandler.EventHandler;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class HotArticleScoreUpdaterTest {

    @InjectMocks
    private HotArticleScoreUpdater hotArticleScoreUpdater;
    @Mock
    HotArticleListRepository hotArticleListRepository;
    @Mock
    HotArticleScoreCalculator hotArticleScoreCalculator;
    @Mock
    ArticleCreatedTimeRepository articleCreatedTimeRepository;

    @DisplayName("오늘 생성되지 않은 Article update시 hanler의 handle을 호출하지 않고 redis에 저장하지 않는다.")
    @Test
    void updateIfArticleNotCreatedToday() {
        // given
        Long articleId = 1L;
        Event event = mock(Event.class);
        EventHandler eventHandler = mock(EventHandler.class);

        BDDMockito.given(eventHandler.findArticleId(event)).willReturn(articleId);
        LocalDateTime created = LocalDateTime.now().minusDays(1);
        BDDMockito.given(articleCreatedTimeRepository.read(articleId)).willReturn(created);

        // when
        hotArticleScoreUpdater.update(event, eventHandler);

        // then
        BDDMockito.verify(eventHandler, never()).handle(event);
        BDDMockito.verify(hotArticleListRepository, never()).add(anyLong(), any(LocalDateTime.class)
                , anyLong(), anyLong(), any(Duration.class));

    }

    @DisplayName("오늘 생성된 Article 은 정상적으로 update 된다.")
    @Test
    void updateTest() {
        // given
        Long articleId = 1L;
        Event event = mock(Event.class);
        EventHandler eventHandler = mock(EventHandler.class);

        BDDMockito.given(eventHandler.findArticleId(event)).willReturn(articleId);
        LocalDateTime created = LocalDateTime.now();
        BDDMockito.given(articleCreatedTimeRepository.read(articleId)).willReturn(created);

        // when
        hotArticleScoreUpdater.update(event, eventHandler);

        // then
        BDDMockito.verify(eventHandler).handle(event);
        BDDMockito.verify(hotArticleListRepository).add(anyLong(), any(LocalDateTime.class)
                , anyLong(), anyLong(), any(Duration.class));
    }
}
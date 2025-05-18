package isluel.edb.hotarticle.service;

import isluel.edb.common.event.Event;
import isluel.edb.common.event.EventPayload;
import isluel.edb.hotarticle.repository.ArticleCreatedTimeRepository;
import isluel.edb.hotarticle.repository.HotArticleListRepository;
import isluel.edb.hotarticle.service.evnetHandler.EventHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;

// kafka로부터 전달받은 Event 데이터를 처리하는 Service
@Component
@Slf4j
@RequiredArgsConstructor
public class HotArticleScoreUpdater {
    private final HotArticleListRepository hotArticleListRepository;
    private final HotArticleScoreCalculator hotArticleScoreCalculator;
    // 오늘 생성된 게시글인지 확인.
    private final ArticleCreatedTimeRepository articleCreatedTimeRepository;

    // 인기글은 하루 10개만 저장.
    private static final long HOT_ARTICLE_COUNT = 10;
    // 인기글은 10일동안만 저장
    private static final Duration HOT_ARTICLE_TTL = Duration.ofDays(10);

    public void update(Event<EventPayload> event, EventHandler<EventPayload> handler) {
        var articleId = handler.findArticleId(event);
        var created = articleCreatedTimeRepository.read(articleId);

        // 오늘 생성된 게시글이 아니면 이벤트 처리하지 않는다.
        if (!isArticleCreatedToday(created)) {
            return;
        }

        // 이벤트 정보를 Redis에 저장.
        handler.handle(event);

        // 인기글의 점수 계산
        long score = hotArticleScoreCalculator.calculate(articleId);
        hotArticleListRepository.add(articleId, created, score, HOT_ARTICLE_COUNT, HOT_ARTICLE_TTL);
    }

    private boolean isArticleCreatedToday(LocalDateTime created) {
        return created != null && created.toLocalDate().equals(LocalDate.now());
    }
}

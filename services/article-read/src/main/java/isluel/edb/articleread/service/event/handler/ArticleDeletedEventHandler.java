package isluel.edb.articleread.service.event.handler;

import isluel.edb.articleread.repository.ArticleIdListRepository;
import isluel.edb.articleread.repository.ArticleQueryModelRepository;
import isluel.edb.articleread.repository.BoardArticleCountRepository;
import isluel.edb.common.event.Event;
import isluel.edb.common.event.EventType;
import isluel.edb.common.event.payload.ArticleDeletedEventPayload;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ArticleDeletedEventHandler implements EventHandler<ArticleDeletedEventPayload> {
    private final ArticleQueryModelRepository articleQueryModelRepository;
    private final ArticleIdListRepository articleIdListRepository;
    private final BoardArticleCountRepository boardArticleCountRepository;

    @Override
    public void handle(Event<ArticleDeletedEventPayload> event) {
        // 기존 데이터 읽어오기
        var payload = event.getPayload();
//        articleQueryModelRepository.delete(payload.getArticleId());

        // 순서 중요
        // 찰나의 순간이지만 articleQueryModelRepository이 먼저 삭제되면,
        // 목록(ArticleId LIst)에는 데이터가 있는데 QueryModel에 없어서 조회시 없는 걸로 뜰수 있음..
        articleIdListRepository.delete(payload.getBoardId(), payload.getArticleId());
        articleQueryModelRepository.delete(payload.getArticleId());
        boardArticleCountRepository.createOrUpdate(payload.getBoardId(), payload.getBoardArticleCount());
    }

    @Override
    public boolean supports(Event<ArticleDeletedEventPayload> event) {
        return EventType.ARTICLE_DELETED.equals(event.getType());
    }
}

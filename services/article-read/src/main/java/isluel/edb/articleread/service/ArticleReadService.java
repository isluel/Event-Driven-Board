package isluel.edb.articleread.service;

import isluel.edb.articleread.client.*;
import isluel.edb.articleread.repository.ArticleIdListRepository;
import isluel.edb.articleread.repository.ArticleQueryModel;
import isluel.edb.articleread.repository.ArticleQueryModelRepository;
import isluel.edb.articleread.repository.BoardArticleCountRepository;
import isluel.edb.articleread.service.event.handler.EventHandler;
import isluel.edb.articleread.service.response.ArticleReadPageResponse;
import isluel.edb.articleread.service.response.ArticleReadResponse;
import isluel.edb.common.event.Event;
import isluel.edb.common.event.EventPayload;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class ArticleReadService {
    private final ArticleClient articleClient;
    private final CommentClient commentClient;
    private final LikeClient likeClient;
    private final ViewClient viewClient;
    private final HateClient hateClient;
    // Article 데이터가 저장되어있음.
    private final ArticleQueryModelRepository articleQueryModelRepository;
    private final List<EventHandler> eventHandlers;

    private final ArticleIdListRepository articleIdListRepository;
    private final BoardArticleCountRepository boardArticleCountRepository;

    public void handleEvent(Event<EventPayload> event) {
        for (var handler : eventHandlers) {
            if (handler.supports(event)) {
                handler.handle(event);
            }
        }
    }

    // Article 게시글을 조회하기 위한 메서드
    // 없으면 rest client를 통해 원본 데이터 호출
    public ArticleReadResponse read(Long articleId) {
        var articleQueryModel = articleQueryModelRepository.read(articleId)
                .or(() -> fetch(articleId))
                .orElseThrow();

        return ArticleReadResponse.from(
                articleQueryModel,
                viewClient.count(articleId)
        );
    }

    public Optional<ArticleQueryModel> fetch(Long articleId) {
        // 게시글 조회
        var articleQueryModel = articleClient.read(articleId)
                .map(article -> ArticleQueryModel.create(
                        article,
                        commentClient.count(articleId),
                        likeClient.count(articleId),
                        hateClient.count(articleId)
                ));
        // 가져온글 Redis에 저장.
        articleQueryModel
                .ifPresent(articleQuery ->
                        articleQueryModelRepository.create(articleQuery, Duration.ofDays(1)));
        log.info("[ArticleReadService.fetch] data articleId={}, isPresent={}", articleQueryModel, articleQueryModel.isPresent());

        return articleQueryModel;
    }

    // 페이징 처리
    public ArticleReadPageResponse readAll(Long boardId, Long page, Long pageSize) {
        return ArticleReadPageResponse.of(
                readAll(
                        readAllArticleIds(boardId, page, pageSize)
                ),
                count(boardId)
        );
    }

    // articleId List에 해당되는 데이터를 가져옴.
    public List<ArticleReadResponse> readAll(List<Long> articleIds) {
        Map<Long, ArticleQueryModel> map = articleQueryModelRepository.readAll(articleIds);
        return articleIds.stream()
                .map(articleId -> map.containsKey(articleId)
                        ? map.get(articleId)
                            : fetch(articleId).orElse(null))
                .filter(Objects::nonNull)
                .map(articleQueryModel -> ArticleReadResponse.from(
                        articleQueryModel,
                        viewClient.count(articleQueryModel.getArticleId())
                ))
                .toList();
    }

    // boardId, page, pageSie에 해당되는 article Id 목록을 전달해줌
    public List<Long> readAllArticleIds(Long boardId, Long page, Long pageSize) {
        var articleIds = articleIdListRepository.readAll(boardId, (page - 1) * pageSize, pageSize);
        // Redis 에 다 저장되어있는 경우
        if (pageSize == articleIds.size()) {
            log.info("[ArticleReadService.readAllArticleIds] return from redis");
            return articleIds;
        }

        // 원본 데이터에서 가져옴
        log.info("[ArticleReadService.readAllArticleIds] return from origin data");
        return articleClient.readAll(boardId, page, pageSize).getArticles()
                .stream().map(ArticleClient.ArticleResponse::getArticleId)
                .toList();

    }

    // redis에 저장된 board의 article Count를 가져온다.
    // 없으면 원본데이터를 가져와 Redis에 저장한다.
    private Long count(Long boardId) {
        var result = boardArticleCountRepository.read(boardId);

        if (result != null) {
            return result;
        }

        var count = articleClient.count(boardId);
        boardArticleCountRepository.createOrUpdate(boardId, count);
        return count;
    }

    public List<ArticleReadResponse> readAllInfiniteScroll(Long boardId, Long lastArticleId, Long pageSize) {
        return readAll(
                readAllInfiniteScrollArticleIds(boardId, lastArticleId, pageSize)
        );
    }

    private List<Long> readAllInfiniteScrollArticleIds(Long boardId, Long lastArticleId, Long pageSize) {
        var ids = articleIdListRepository.readAllInfiniteScroll(boardId, lastArticleId, pageSize);
        if (pageSize == ids.size()) {
            log.info("[ArticleReadService.readAllInfiniteScrollArticleIds] return from redis");
            return ids;
        }

        log.info("[ArticleReadService.readAllInfiniteScrollArticleIds] return from origin data");
        return articleClient.readAllInfiniteScroll(boardId, lastArticleId, pageSize)
                .stream().map(ArticleClient.ArticleResponse::getArticleId)
                .toList();
    }


}

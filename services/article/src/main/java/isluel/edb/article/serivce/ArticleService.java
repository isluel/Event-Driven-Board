package isluel.edb.article.serivce;

import isluel.edb.article.entity.Article;
import isluel.edb.article.entity.BoardArticleCount;
import isluel.edb.article.repository.ArticleRepository;
import isluel.edb.article.repository.BoardArticleCountRepository;
import isluel.edb.article.serivce.request.ArticleCreateRequest;
import isluel.edb.article.serivce.request.ArticleUpdateRequest;
import isluel.edb.article.serivce.response.ArticlePageResponse;
import isluel.edb.article.serivce.response.ArticleResponse;
import isluel.edb.common.event.EventType;
import isluel.edb.common.event.payload.ArticleCreatedEventPayload;
import isluel.edb.common.event.payload.ArticleDeletedEventPayload;
import isluel.edb.common.event.payload.ArticleUpdatedPayload;
import isluel.edb.common.outboxmessagingrelay.OutboxEventPublisher;
import isluel.edb.common.snowflake.Snowflake;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ArticleService {

    private final Snowflake snowflake = new Snowflake();
    private final ArticleRepository articleRepository;
    private final OutboxEventPublisher outboxEventPublisher;
    private final BoardArticleCountRepository boardArticleCountRepository;

    @Transactional
    public ArticleResponse create(ArticleCreateRequest request) {
        Article article = articleRepository.save(
                Article.create(snowflake.nextId()
                        , request.getTitle(), request.getContent(), request.getBoardId(), request.getWriterId())
        );

        var result = boardArticleCountRepository.increase(article.getBoardId());
        if (result == 0) {
            boardArticleCountRepository.save(
                    BoardArticleCount.init(request.getBoardId(), 1L)
            );
        }

        outboxEventPublisher.publish(
                EventType.ARTICLE_CREATED,
                ArticleCreatedEventPayload.builder()
                        .articleId(article.getArticleId())
                        .title(article.getTitle())
                        .content(article.getContent())
                        .boardId(article.getBoardId())
                        .writerId(article.getWriterId())
                        .createdAt(article.getCreatedAt())
                        .modifiedAt(article.getModifiedAt())
                        .boardArticleCount(count(article.getBoardId()))
                        .build(),
                article.getArticleId()
                );

        return ArticleResponse.from(article);
    }

    @Transactional
    public ArticleResponse update(Long articleId, ArticleUpdateRequest request) {
        var article = articleRepository.findById(articleId).orElseThrow();
        article.update(request.getTitle(), request.getContent());
        outboxEventPublisher.publish(
                EventType.ARTICLE_UPDATED,
                ArticleUpdatedPayload.builder()
                        .articleId(article.getArticleId())
                        .title(article.getTitle())
                        .content(article.getContent())
                        .boardId(article.getBoardId())
                        .writerId(article.getWriterId())
                        .createdAt(article.getCreatedAt())
                        .modifiedAt(article.getModifiedAt())
                        .build(),
                article.getArticleId()
        );
        return ArticleResponse.from(article);
    }

    public ArticleResponse read(Long articleId) {
        return ArticleResponse.from(articleRepository.findById(articleId).orElseThrow());
    }

    @Transactional
    public void delete(Long articleId) {
        var article = articleRepository.findById(articleId)
                .orElseThrow();
        articleRepository.delete(article);

        boardArticleCountRepository.decrease(article.getBoardId());

        outboxEventPublisher.publish(
                EventType.ARTICLE_DELETED,
                ArticleDeletedEventPayload.builder()
                        .articleId(article.getArticleId())
                        .title(article.getTitle())
                        .content(article.getContent())
                        .boardId(article.getBoardId())
                        .writerId(article.getWriterId())
                        .createdAt(article.getCreatedAt())
                        .modifiedAt(article.getModifiedAt())
                        .boardArticleCount(count(article.getBoardId()))
                        .build(),
                article.getArticleId()
        );
    }

    public ArticlePageResponse readAll(Long boardId, Long page, Long pageSize) {
        return ArticlePageResponse.of(
                articleRepository.findAll(boardId, (page - 1) * pageSize, pageSize)
                        .stream().map(ArticleResponse::from)
                        .toList(),
                articleRepository.count(boardId,
                        PageLimitCalculator.calculateArticleLimit(page, pageSize, 10L))
        );
    }

    public List<ArticleResponse> readAllInfiniteScroll(Long boardId, Long pageSize, Long lastArticleId) {
        return (lastArticleId == null ?
                articleRepository.findAllInfiniteScroll(boardId, pageSize)
                : articleRepository.findAllInfiniteScroll(boardId, pageSize, lastArticleId)
                )
                .stream().map(ArticleResponse::from)
                .toList();
    }

    public Long count(Long boardId) {
        return boardArticleCountRepository.findById(boardId)
                .map(BoardArticleCount::getArticleCount)
                .orElse(0L);
    }
}

package isluel.edb.hate.service;

import isluel.edb.common.event.EventType;
import isluel.edb.common.event.payload.ArticleLikedEventPayload;
import isluel.edb.common.outboxmessagingrelay.OutboxEventPublisher;
import isluel.edb.common.snowflake.Snowflake;
import isluel.edb.hate.entity.ArticleHate;
import isluel.edb.hate.entity.ArticleHateCount;
import isluel.edb.hate.repository.ArticleHateCountRepository;
import isluel.edb.hate.repository.ArticleHateRepository;
import isluel.edb.hate.service.response.ArticleHateResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ArticleHateService {
    private final ArticleHateRepository articleHateRepository;
    private final Snowflake snowflake = new Snowflake();
    private final ArticleHateCountRepository ArticleHateCountRepository;
    private final OutboxEventPublisher outboxEventPublisher;


    public ArticleHateResponse read(Long articleId, Long userId) {
        return articleHateRepository.findByArticleIdAndUserId(articleId, userId)
                .map(ArticleHateResponse::from)
                .orElseThrow();
    }


    @Transactional
    public void hate(Long articleId, Long userId) {
        var articleLike = articleHateRepository.save(
                ArticleHate.create(
                        snowflake.nextId(),
                        articleId,
                        userId
                )
        );

        int result = ArticleHateCountRepository.increase(articleId);
        // insert 데이터가 없는 경우
        if (result == 0) {
            // 최초 요청 시에는 update 되는 레코드가 없으므로 1로 초기화.
            // 트래픽이 순산간이 몰릴 경우 유실될수 있으므로, 게시글 생성시 미리 0으로 초기화 하는것이 나음
            ArticleHateCountRepository.save(
                    ArticleHateCount.init(articleId, 1L)
            );
        }

        outboxEventPublisher.publish(EventType.ARTICLE_HATE,
                ArticleLikedEventPayload.builder()
                        .articleLikeId(articleLike.getArticleHateId())
                        .articleId(articleLike.getArticleId())
                        .userId(articleLike.getUserId())
                        .createdAt(articleLike.getCreatedAt())
                        .articleLikeCount(count(articleLike.getArticleHateId()))
                        .build(),
                articleId
        );
    }

    @Transactional
    public void unHate(Long articleId, Long userId) {
        articleHateRepository.findByArticleIdAndUserId(articleId, userId)
                .ifPresent(articleLike -> {
                    articleHateRepository.delete(articleLike);
                    ArticleHateCountRepository.decrease(articleId);

                    outboxEventPublisher.publish(EventType.ARTICLE_UNHATE,
                            ArticleLikedEventPayload.builder()
                                    .articleLikeId(articleLike.getArticleHateId())
                                    .articleId(articleLike.getArticleId())
                                    .userId(articleLike.getUserId())
                                    .createdAt(articleLike.getCreatedAt())
                                    .articleLikeCount(count(articleLike.getArticleHateId()))
                                    .build(),
                            articleId
                    );
                });
    }

    public Long count(Long articleId) {
        return ArticleHateCountRepository.findById(articleId)
                .map(ArticleHateCount::getHateCount)
                .orElse(0L);
    }
}

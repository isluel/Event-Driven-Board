package isluel.edb.like.service;

import isluel.edb.common.event.EventType;
import isluel.edb.common.event.payload.ArticleLikedEventPayload;
import isluel.edb.common.outboxmessagingrelay.OutboxEventPublisher;
import isluel.edb.common.snowflake.Snowflake;
import isluel.edb.like.entity.ArticleLike;
import isluel.edb.like.entity.ArticleLikeCount;
import isluel.edb.like.repository.ArticleLikeCountRepository;
import isluel.edb.like.repository.ArticleLikeRepository;
import isluel.edb.like.service.response.ArticleLikeResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ArticleLikeService {
    private final ArticleLikeRepository articleLikeRepository;
    private final Snowflake snowflake = new Snowflake();
    private final ArticleLikeCountRepository articleLikeCountRepository;
    private final OutboxEventPublisher outboxEventPublisher;


    public ArticleLikeResponse read(Long articleId, Long userId) {
        return articleLikeRepository.findByArticleIdAndUserId(articleId, userId)
                .map(ArticleLikeResponse::from)
                .orElseThrow();
    }


    @Transactional
    public void like(Long articleId, Long userId) {
        var articleLike = articleLikeRepository.save(
                ArticleLike.create(
                        snowflake.nextId(),
                        articleId,
                        userId
                )
        );

        int result = articleLikeCountRepository.increase(articleId);
        // insert 데이터가 없는 경우
        if (result == 0) {
            // 최초 요청 시에는 update 되는 레코드가 없으므로 1로 초기화.
            // 트래픽이 순산간이 몰릴 경우 유실될수 있으므로, 게시글 생성시 미리 0으로 초기화 하는것이 나음
            articleLikeCountRepository.save(
                    ArticleLikeCount.init(articleId, 1L)
            );
        }

        outboxEventPublisher.publish(EventType.ARTICLE_LIKED,
                ArticleLikedEventPayload.builder()
                        .articleLikeId(articleLike.getArticleLikeId())
                        .articleId(articleLike.getArticleId())
                        .userId(articleLike.getUserId())
                        .createdAt(articleLike.getCreatedAt())
                        .articleLikeCount(count(articleLike.getArticleLikeId()))
                        .build(),
                articleId
        );
    }

    @Transactional
    public void unlike(Long articleId, Long userId) {
        articleLikeRepository.findByArticleIdAndUserId(articleId, userId)
                .ifPresent(articleLike -> {
                    articleLikeRepository.delete(articleLike);
                    articleLikeCountRepository.decrease(articleId);

                    outboxEventPublisher.publish(EventType.ARTICLE_UNLIKED,
                            ArticleLikedEventPayload.builder()
                                    .articleLikeId(articleLike.getArticleLikeId())
                                    .articleId(articleLike.getArticleId())
                                    .userId(articleLike.getUserId())
                                    .createdAt(articleLike.getCreatedAt())
                                    .articleLikeCount(count(articleLike.getArticleLikeId()))
                                    .build(),
                            articleId
                    );
                });
    }

    public Long count(Long articleId) {
        return articleLikeCountRepository.findById(articleId)
                .map(ArticleLikeCount::getLikeCount)
                .orElse(0L);
    }
}

package isluel.edb.hotarticle.service;

import isluel.edb.hotarticle.repository.ArticleCommentCountRepository;
import isluel.edb.hotarticle.repository.ArticleHateCountRepository;
import isluel.edb.hotarticle.repository.ArticleLikeCountRepository;
import isluel.edb.hotarticle.repository.ArticleViewCountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

// 인기글 점수를 계산해주는 Service
@Component
@RequiredArgsConstructor
public class HotArticleScoreCalculator {
    private final ArticleLikeCountRepository articleLikeCountRepository;
    private final ArticleHateCountRepository articleHateCountRepository;
    private final ArticleCommentCountRepository articleCommentRepository;
    private final ArticleViewCountRepository articleViewCountRepository;

    // 계산시 사용할 가중치 상수
    private static final long ARTICLE_LIKE_COUNT_WEIGHT = 3;
    private static final long ARTICLE_HATE_COUNT_WEIGHT = 2;
    private static final long ARTICLE_COMMENT_COUNT_WEIGHT = 2;
    private static final long ARTICLE_VIEW_COUNT_WEIGHT = 1;


    public long calculate(Long articleId) {
        var articleLikeCount = articleLikeCountRepository.read(articleId);
        var articleHateCount = articleHateCountRepository.read(articleId);
        var articleViewCount = articleViewCountRepository.read(articleId);
        var articleCommentCount = articleCommentRepository.read(articleId);

        return articleLikeCount * ARTICLE_LIKE_COUNT_WEIGHT
                + articleViewCount * ARTICLE_VIEW_COUNT_WEIGHT
                + articleCommentCount * ARTICLE_COMMENT_COUNT_WEIGHT
                - articleHateCount * ARTICLE_HATE_COUNT_WEIGHT
                ;
    }
}

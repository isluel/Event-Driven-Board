package isluel.edb.hotarticle.service;

import isluel.edb.hotarticle.repository.ArticleCommentCountRepository;
import isluel.edb.hotarticle.repository.ArticleHateCountRepository;
import isluel.edb.hotarticle.repository.ArticleLikeCountRepository;
import isluel.edb.hotarticle.repository.ArticleViewCountRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.random.RandomGenerator;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class HotArticleScoreCalculatorTest {
    @InjectMocks
    HotArticleScoreCalculator hotArticleScoreCalculator;
    @Mock
    ArticleLikeCountRepository articleLikeCountRepository;
    @Mock
    ArticleHateCountRepository articleHateCountRepository;
    @Mock
    ArticleViewCountRepository articleViewCountRepository;
    @Mock
    ArticleCommentCountRepository articleCommentCountRepository;

    @Test
    void calculate() {
        // given
        Long articleId = 1L;
        var likeCount =  RandomGenerator.getDefault().nextLong(100);
        var hateCount =  RandomGenerator.getDefault().nextLong(50);
        var commentCount =  RandomGenerator.getDefault().nextLong(100);
        var viewCount =  RandomGenerator.getDefault().nextLong(100);
        BDDMockito.given(articleLikeCountRepository.read(articleId)).willReturn(likeCount);
        BDDMockito.given(articleViewCountRepository.read(articleId)).willReturn(viewCount);
        BDDMockito.given(articleCommentCountRepository.read(articleId)).willReturn(commentCount);
        BDDMockito.given(articleHateCountRepository.read(articleId)).willReturn(hateCount);

        // when
        long score = hotArticleScoreCalculator.calculate(articleId);

        // then
        assertThat(score)
                .isEqualTo(3 * likeCount + 2* commentCount + 1 *viewCount - 2 * hateCount);
    }
}
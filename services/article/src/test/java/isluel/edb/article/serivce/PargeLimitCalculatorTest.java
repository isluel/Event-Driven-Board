package isluel.edb.article.serivce;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Slf4j
class PargeLimitCalculatorTest {

    @Test
    void calculatePageLimitTest() {
        calculatePageLimitTest(1L, 30L, 10L, 301L);
        calculatePageLimitTest(7L, 30L, 10L, 301L);
        calculatePageLimitTest(10L, 30L, 10L, 301L);

        calculatePageLimitTest(11L, 30L, 10L, 601L);
        calculatePageLimitTest(12L, 30L, 10L, 601L);
    }


    void calculatePageLimitTest(Long page, Long pagSize, Long movablePagCount, Long expected) {
        Long result = PageLimitCalculator.calculateArticleLimit(page, pagSize, movablePagCount);

        assertThat(result).isEqualTo(expected);
    }
}
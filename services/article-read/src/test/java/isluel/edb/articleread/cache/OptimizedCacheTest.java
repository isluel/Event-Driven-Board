package isluel.edb.articleread.cache;

import lombok.*;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

class OptimizedCacheTest {

    @Test
    void parseDataTest() {
        parseDataTest("data", 10);
        parseDataTest(3L, 10);
        parseDataTest(3, 10);
        parseDataTest(new TestClass("Test"), 10);
    }

    void parseDataTest(Object data, long ttlSeconds) {
        // given
        var cache = OptimizedCache.of(data, Duration.ofSeconds(ttlSeconds));
        System.out.println("cache: " + cache);

        // when
        Object d = cache.parseData(data.getClass());

        // then
        System.out.println("d: " + d);
        assertThat(d).isEqualTo(data);
    }

    @Getter
    @ToString
    @EqualsAndHashCode
    @NoArgsConstructor
    @AllArgsConstructor
    static class TestClass{
        String testData;
    }

    @Test
    void isExpiredTest() {
        assertThat(OptimizedCache.of("data", Duration.ofDays(-30)).isExpired()).isTrue();
        assertThat(OptimizedCache.of("data", Duration.ofDays(30)).isExpired()).isFalse();
    }

}
package isluel.edb.articleread.cache;

import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

class OptimizedCacheTTLTest {

    @Test
    void ofTest() {
        // given
        long ttlSecond = 10;

        // when
        OptimizedCacheTTL optimizedCacheTTL = OptimizedCacheTTL.of(ttlSecond);

        // then
        assertThat(optimizedCacheTTL.getLocalTTL()).isEqualTo(Duration.ofSeconds(ttlSecond));
        assertThat(optimizedCacheTTL.getPhysicalTTL()).isEqualTo(Duration.ofSeconds(ttlSecond).plusSeconds(OptimizedCacheTTL.PHYSICAL_TTL_SECONDS));
    }
}
package isluel.edb.articleread.cache;

import lombok.Getter;

import java.time.Duration;

@Getter
public class OptimizedCacheTTL {
    private Duration localTTL;
    private Duration physicalTTL;

    public static final long PHYSICAL_TTL_SECONDS = 5;

    public static OptimizedCacheTTL of(long localTTL) {
        OptimizedCacheTTL optimizedCacheTTL = new OptimizedCacheTTL();
        optimizedCacheTTL.localTTL = Duration.ofSeconds(localTTL);
        optimizedCacheTTL.physicalTTL = optimizedCacheTTL.localTTL.plusSeconds(PHYSICAL_TTL_SECONDS);
        return optimizedCacheTTL;
    }
}

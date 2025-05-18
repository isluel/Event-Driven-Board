package isluel.edb.articleread.cache;

import isluel.edb.common.dataserialiser.DataSerializer;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class OptimizedCacheManager {
    private final StringRedisTemplate redisTemplate;
    private final OptimizedCacheLockProvider optimizedCacheLockProvider;

    private static final String DELIMITER = "::";

    // 캐시가 어떤 타입인지
    // 캐시에 대해 유니크하게 구분하기 위한 파라미터,
    // 오브잭트에 대한 Return Type
    // 원본데이터를 가져오기 위한 OptimizedCacheOriginDataSupplier
    public Object process(String type, long ttlSecond, Object[] args, Class<?> returnType,
                          OptimizedCacheOriginDataSupplier<?> supplier) throws Throwable{
        String key = generateKey(type, args);
        // key에 대한 데이터를 가져옴
        var cachedData = redisTemplate.opsForValue().get(key);
        // 데이터가 없으면 원본데이터 호출
        if(cachedData == null) {
            return refresh(supplier, key, ttlSecond);
        }

        var optimizedCache = DataSerializer.deserialize(cachedData, OptimizedCache.class);
        if (optimizedCache == null) {
            return refresh(supplier, key, ttlSecond);
        }

        // 만료되지 않은 경우 바로 반환
        if (!optimizedCache.isExpired()) {
            return optimizedCache.parseData(returnType);
        }

        // Local 만료시.. lock을 잡고
        // lock 잡을수 있으면 데이터 업데이트
        // lock 못잡으면 그냥 redis 에서 가져온 데이터를 반환
        if (!optimizedCacheLockProvider.lock(key)) {
            return optimizedCache.parseData(returnType);
        }

        try {
            return refresh(supplier, key, ttlSecond);
        } finally {
            optimizedCacheLockProvider.unlock(key);
        }
    }

    // 원본 데이터를 가져온 후
    // 가져온 데이터로 Local, physical TTL을 계산하고,
    // Redis에 physical TTL로 데이터를 저장한다.
    private Object refresh(OptimizedCacheOriginDataSupplier<?> supplier, String key, long ttlSecond) throws Throwable {
        var origin = supplier.get();
        var optimisticCacheTTL = OptimizedCacheTTL.of(ttlSecond);
        var optimizedCache =  OptimizedCache.of(origin, optimisticCacheTTL.getLocalTTL());

        redisTemplate.opsForValue().set(key, DataSerializer.serialize(optimizedCache), optimisticCacheTTL.getPhysicalTTL());

        return origin;
    }

    // prefix::arg_1::arg_2
    private String generateKey(String prefix, Object[] args) {
        return prefix + DELIMITER +
                Arrays.stream(args).map(Object::toString).collect(Collectors.joining(DELIMITER));
    }
}

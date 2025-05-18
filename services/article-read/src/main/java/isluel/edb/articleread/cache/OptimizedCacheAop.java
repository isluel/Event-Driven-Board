package isluel.edb.articleread.cache;

import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

@Aspect
@Component
@RequiredArgsConstructor
public class OptimizedCacheAop {
    private final OptimizedCacheManager optimizedCacheManager;

    // OptimizedCacheable 어노케이션이 붙은 항목에 대한 처리
    @Around("@annotation(OptimizedCacheable)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        OptimizedCacheable cacheable = findAnnotation(joinPoint);

        return optimizedCacheManager.process(cacheable.type(), cacheable.ttpSeconds(), joinPoint.getArgs()
                , findReturnType(joinPoint), () -> joinPoint.proceed());
    }

    // 수행 메서드에서 OptimizedCacheable가 달려있는지 찾아올수 있다.
    private OptimizedCacheable findAnnotation(ProceedingJoinPoint joinPoint) {
        var signature = joinPoint.getSignature();
        var methodSignature = (MethodSignature) signature;
        return methodSignature.getMethod().getAnnotation(OptimizedCacheable.class);
    }

    // 반환 타입
    private Class<?> findReturnType(ProceedingJoinPoint joinPoint) {
        var signature = joinPoint.getSignature();
        var methodSignature = (MethodSignature) signature;
        return methodSignature.getReturnType();
    }
}

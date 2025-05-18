package isluel.edb.hotarticle.utils;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;

// 인기글은 당일 데이터만 저장한다.
// 인기글 관련 count들 정보를 Redis에 저장할때 넣을 TTL을 계산할때 사용한다.
public class TimeCalculatorUtils {

    // 현재 시간에서 자정까지 얼마나 시간이 남았는지 반환해주는 메서드
    public static Duration calculationDurationToMidnight() {
        LocalDateTime now = LocalDateTime.now();
        var midnight = now.plusDays(1).with(LocalTime.MIDNIGHT);

        return Duration.between(now, midnight);
    }
}

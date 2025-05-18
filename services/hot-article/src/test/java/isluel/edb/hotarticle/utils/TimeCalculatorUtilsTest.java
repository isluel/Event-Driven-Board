package isluel.edb.hotarticle.utils;

import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

class TimeCalculatorUtilsTest {
    @Test
    void test() {
        Duration duration = TimeCalculatorUtils.calculationDurationToMidnight();
        System.out.println("duration.get = " + duration.getSeconds() / 60);
    }
}
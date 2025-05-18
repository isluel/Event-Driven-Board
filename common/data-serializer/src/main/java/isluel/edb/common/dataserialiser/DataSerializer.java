package isluel.edb.common.dataserialiser;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DataSerializer {
    private static final ObjectMapper objectMapper = initialize();

    public static ObjectMapper initialize() {
        return new ObjectMapper()
                // 시간 관련 처리 모듈 추가
                .registerModule(new JavaTimeModule())
                // 역직렬화 시 없는 필드는 에러 발생 안하고 무시한다.
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    // String Type 의 Data를 받아서 clazz 로 역직렬화
    public static <T> T deserialize(String data, Class<T> clazz) {
        try {
            return objectMapper.readValue(data, clazz);
        } catch (JsonProcessingException e) {
            log.error("[DataSerializer.deserialize] data = {}, clazz = {}", data, clazz, e);
            // 에러시 null 반환
            return null;
        }
    }

    // Object Type의 데이터를 clazz로변환
    public static <T> T deserialize(Object data, Class<T> clazz) {
        return objectMapper.convertValue(data, clazz);
    }

    // Object data를 String 으로 직렬화
    public static String serialize(Object data) {
        try {
            return objectMapper.writeValueAsString(data);
        } catch (JsonProcessingException e) {
            log.error("[DataSerializer.serialize] object = {}", data, e);
            return null;
        }
    }
}

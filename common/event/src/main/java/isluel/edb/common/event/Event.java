package isluel.edb.common.event;

import isluel.edb.common.dataserialiser.DataSerializer;
import lombok.Getter;

@Getter
public class Event<T extends EventPayload> {
    private Long eventId;
    // Event의 Type
    private EventType type;
    // Event의 실제 데이터
    private T payload;

    public static Event<EventPayload> of(Long eventId, EventType type, EventPayload payload) {
        Event<EventPayload> event = new Event<>();
        event.eventId = eventId;
        event.type = type;
        event.payload = payload;
        return event;
    }

    // kafka로 전달할때 직렬화
    public String toJson() {
        return DataSerializer.serialize(this);
    }
    
    // kafka 로 전달 받은 데이터를 역직렬화
    public static Event<EventPayload> fromJson(String json) {
        // 먼저 EventRaw로 변경
        var eventRaw = DataSerializer.deserialize(json, EventRaw.class);
        if (eventRaw == null) {
            return null;
        }

        // EventRaw를 Event 객체로 변환
        // kafka의 topic에 따라서 payload가 다름!
        // event Type에 맞춰서 payload를 역직렬화 해야한다.
        Event<EventPayload> event = new Event<>();
        event.eventId = eventRaw.eventId;
        event.type = EventType.from(eventRaw.getType());
        event.payload = DataSerializer.deserialize(eventRaw.getPayload(), event.type.getPayloadClass());
        return event;
    }

    // kafka 에서 전달 받은 데이터
    @Getter
    private static class EventRaw {
        private Long eventId;
        private String type;
        private Object payload;
    }
}

package isluel.edb.common.outboxmessagingrelay;

import isluel.edb.common.event.Event;
import isluel.edb.common.event.EventPayload;
import isluel.edb.common.event.EventType;
import isluel.edb.common.snowflake.Snowflake;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OutboxEventPublisher {
    private final Snowflake outboxIdSnowFlake = new Snowflake();
    private final Snowflake eventIdFlake = new Snowflake();
    private final ApplicationEventPublisher applicationEventPublisher;

    // 각 Service 에서 Event Publisher를 통해 이벤트를 발행
    public void publish(EventType eventType, EventPayload payload, Long shardKey) {
        var outbox=  Outbox.create(
                outboxIdSnowFlake.nextId(),
                eventType,
                Event.of(
                        eventIdFlake.nextId(), eventType, payload
                ).toJson(),
                // article id 가 10으로 들어올 경우.
                // 해당 Article id의 shard가 어디있는지 계산하기 위해 사용
                shardKey % MessageRelayConstants.SHARD_COUNT
        );

        // 메시지 relay에서 수신해서 처리하도록 한다.
        applicationEventPublisher.publishEvent(OutboxEvent.of(outbox));
    }
}

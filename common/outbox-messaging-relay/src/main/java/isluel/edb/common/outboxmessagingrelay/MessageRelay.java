package isluel.edb.common.outboxmessagingrelay;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class MessageRelay {

    private final OutboxRepository outboxRepository;
    // 살이있는 Application 확인 및 자신에게 할당된 Shrad List 조회가능
    private final MessageRelayCoordinator messageRelayCoordinator;
    private final KafkaTemplate<String, String> kafkaTemplate;

    // commit 되기전에 outbox 이벤트를 받으면
    // outbox repository 에 저장
    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void createOutbox(OutboxEvent outboxEvent) {
        log.info("[MessageRelay.createOutbox] outboxEvent={}", outboxEvent);
        outboxRepository.save(outboxEvent.getOutbox());
    }

    // Commit 된 후 비동기로 kafka 이벤트 전송
    @Async("messsageRelayPublishEventExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void publishEvent(OutboxEvent outboxEvent) {
        publishEvent(outboxEvent.getOutbox());
    }

    private void publishEvent(Outbox outbox) {
        try {
            kafkaTemplate.send(
                    outbox.getEventType().getTopic(),
                    String.valueOf(outbox.getShardKey()),    // Kafka 전송 키.. 같은 값이 같은 Shard 이고 같은 Partition에서 처리됨.
                    outbox.getPayload()
            ).get(1, TimeUnit.SECONDS);
            outboxRepository.delete(outbox);
        } catch (Exception e) {
            log.error("[MessageRelay.publishEvent] outbox={}", outbox, e);
        }
    }

    // 10초동안 전송 안된 데이터를 주기적으로 Polling 해서 전달
    @Scheduled(fixedRate = 10, initialDelay = 5, timeUnit = TimeUnit.SECONDS, scheduler = "messageRelayPublishPendingEventExecutor")
    public void publishPendingEvents() {
        var assignedShard = messageRelayCoordinator.assignedShards();
        log.info("[MessageRelay.publishPendingEvents] assignedShards size={}", assignedShard.getShards().size());
        // shard를 순회하면서 데이터 전송
        for (Long shard: assignedShard.getShards()) {
            // 지금부터 10초 이후의 데이터 중 전송이되지 않은 항목 중 100개만 조회
            var outboxes = outboxRepository.findAllByShardKeyAndCreatedAtLessThanEqualOrderByCreatedAtAsc(
                    shard,
                    LocalDateTime.now().minusSeconds(10),
                    Pageable.ofSize(100)
            );

            for (Outbox outbox: outboxes) {
                publishEvent(outbox);
            }
        }
    }
}

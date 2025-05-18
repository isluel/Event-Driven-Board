package isluel.edb.hotarticle.consumer;

import isluel.edb.common.event.Event;
import isluel.edb.common.event.EventType;
import isluel.edb.hotarticle.service.HotArticleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class HotArticleEventConsumer {
    private final HotArticleService hotArticleService;

    @KafkaListener(topics = {
            EventType.Topic.BOARD_ARTICLE,
            EventType.Topic.BOARD_COMMENT,
            EventType.Topic.BOARD_LIKE,
            EventType.Topic.BOARD_VIEW,
            EventType.Topic.BOARD_HATE,
    })
    public void consume(String message, Acknowledgment ack) {
        log.info("[HotArticleEventConsumer.consume] message: {}", message);
        var event = Event.fromJson(message);
        if (event != null) {
            hotArticleService.handleEvent(event);
        }

        ack.acknowledge();
    }
}

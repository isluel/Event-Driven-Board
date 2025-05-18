package isluel.edb.articleread.service.event.handler;


import isluel.edb.common.event.Event;
import isluel.edb.common.event.EventPayload;

public interface EventHandler<T extends EventPayload> {
    void handle(Event<T> event);
    boolean supports(Event<T> event);
}

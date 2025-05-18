package isluel.edb.common.event;

import isluel.edb.common.event.payload.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
@RequiredArgsConstructor
public enum EventType {
    ARTICLE_CREATED(ArticleCreatedEventPayload.class, Topic.BOARD_ARTICLE),
    ARTICLE_UPDATED(ArticleUpdatedPayload.class, Topic.BOARD_ARTICLE),
    ARTICLE_DELETED(ArticleDeletedEventPayload.class, Topic.BOARD_ARTICLE),
    COMMENT_CREATED(CommentCreatedEventPayload.class, Topic.BOARD_COMMENT),
    COMMENT_DELETED(CommentDeletedEventPayload.class, Topic.BOARD_COMMENT),
    ARTICLE_LIKED(ArticleLikedEventPayload.class, Topic.BOARD_LIKE),
    ARTICLE_UNLIKED(ArticleUnlikedEventPayload.class, Topic.BOARD_LIKE),
    ARTICLE_VIEWED(ArticleViewEventPayload.class, Topic.BOARD_VIEW),
    ARTICLE_HATE(ArticleHatedEventPayload.class, Topic.BOARD_HATE),
    ARTICLE_UNHATE(ArticleUnhatedEventPayload.class, Topic.BOARD_HATE),
    ;

    // payload에 따른 Class Type 지정
    private final Class<? extends EventPayload> payloadClass;
    // event이 어떤 Topic에 전달될 수있는지.
    private final String topic;

    public static EventType from(String type) {
        try {
            return valueOf(type.toUpperCase());
        } catch (Exception e) {
            log.error("[EventType.from] type={}", type, e);
            return null;
        }
    }

    // Topic 정의
    public static class Topic {
        public static final String BOARD_ARTICLE = "board-article";
        public static final String BOARD_COMMENT = "board-comment";
        public static final String BOARD_LIKE = "board-like";
        public static final String BOARD_VIEW = "board-view";
        public static final String BOARD_HATE = "board-hate";
    }
}

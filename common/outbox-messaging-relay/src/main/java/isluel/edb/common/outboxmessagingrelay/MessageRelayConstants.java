package isluel.edb.common.outboxmessagingrelay;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MessageRelayConstants {
    // 샤딩이 있는것을 상정하고 구현
    // shard 개수를 4개라고 가정하고 구현한다.
    // application 마다 샤드가 분산되어 이벤트를 전송하도록 한다.
    public static final int SHARD_COUNT = 4;
}

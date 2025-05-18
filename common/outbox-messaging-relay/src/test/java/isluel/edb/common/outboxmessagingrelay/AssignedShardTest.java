package isluel.edb.common.outboxmessagingrelay;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class AssignedShardTest {
    @Test
    void ofTest(){
        // given
        Long shardCount = 64L;
        // coorddinator에 3개의 application이 실행중이다.
        var appList = List.of("appId1", "appId2", "appId3");


        // when
        // application 에 샤드를 할당한다.
        var assignedShard1 = AssignedShard.of(appList.get(0), appList, shardCount);
        var assignedShard2 = AssignedShard.of(appList.get(1), appList, shardCount);
        var assignedShard3 = AssignedShard.of(appList.get(2), appList, shardCount);
        // 유효하지 않은 경우 Test
        var assignedShard4 = AssignedShard.of("Invalid", appList, shardCount);

        // then
        var result = Stream.of(
                assignedShard1.getShards(), assignedShard2.getShards(), assignedShard3.getShards(), assignedShard4.getShards()
        ).flatMap(List::stream).toList();
        assertThat(result).hasSize(shardCount.intValue());

        for(var i = 0; i < shardCount; i++) {
            assertThat(result.get(i)).isEqualTo(i);
        }

        assertThat(assignedShard4.getShards()).isEmpty();
    }
}
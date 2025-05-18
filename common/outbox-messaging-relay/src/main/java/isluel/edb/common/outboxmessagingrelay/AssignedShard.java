package isluel.edb.common.outboxmessagingrelay;

import lombok.Getter;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

@Getter
public class AssignedShard {
    private List<Long> shards;

    // Coordinator에 의해 실행되고 있는 Application 목록이 Parameter로 들어온다.
    public static AssignedShard of(String appId, List<String> appIds, long shardCount) {
        AssignedShard shard = new AssignedShard();
        shard.shards = assignShard(appId, appIds, shardCount);
        return shard;
    }

    private static List<Long> assignShard(String appId, List<String> appIds, long shardCount) {
        // 현재 application의 index를 찾는다.
        int appIdx = findAppIndex(appId, appIds);

        // 없으면 할당할 Shard가 없음.
        // 빈 List를 반환
        if (appIdx == -1) {
            return List.of();
        }

        // start ~ end 사이의 범위가 application이 할당된
        // Shard.
        long start = appIdx * shardCount / appIds.size();
        long end = (appIdx + 1) * shardCount/appIds.size() - 1;

        // start~End 길이의 list로 만들어서 반환
        return LongStream.rangeClosed(start, end).boxed().collect(Collectors.toList());
    }

    // 지금 실행된 애플리케이션 목록에서 application Id가 몇번쨰 index에 해당되는지 반환
    private static int findAppIndex(String appId, List<String> appIds) {
        for (int i = 0; i < appIds.size(); i++) {
            if (appId.equals(appIds.get(i))) {
                return i;
            }
        }
        return -1;
    }
}

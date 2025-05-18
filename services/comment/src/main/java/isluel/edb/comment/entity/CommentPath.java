package isluel.edb.comment.entity;

import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@ToString
@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CommentPath {

    private String path;

    private static final String CHARSET = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";

    // Depth의 Path 문자열 길이
    private static final int DEPTH_CHUNK_SIZE = 5;
    // 최대 Depth
    private static final int MAX_DEPTH = 5;

    // 각 Depth의 가장 작은 문자열
    private static final String MIN_CHUNK = String.valueOf(CHARSET.charAt(0)).repeat(DEPTH_CHUNK_SIZE);
    // 각 depth의 가장 큰 문자열
    private static final String MAX_CHUNK = String.valueOf(CHARSET.charAt(CHARSET.length() - 1)).repeat(DEPTH_CHUNK_SIZE);

    public static CommentPath create(String path) {
        // overflow check.
        if (isDepthOverflow(path)) {
            throw new IllegalStateException("Depth overflow");
        }

        CommentPath commentPath = new CommentPath();
        commentPath.path = path;
        return commentPath;
    }

    private static boolean isDepthOverflow(String path) {
        return calDepth(path) > MAX_DEPTH;
    }

    // 길이가 size를 넘으면 안됨.
    private static int calDepth(String path) {
        return path.length() / DEPTH_CHUNK_SIZE;
    }

    // path 의 depth를 계산
    public int getDepth() {
        return calDepth(path);
    }

    // root 확인
    public boolean isRoot() {
        return calDepth(path) == 1;
    }

    // 현재 Path의 parent path 반환
    public String getParentPath() {
        // 마지막 5글자만 빼면됨.
        return path.substring(0, calDepth(path) * DEPTH_CHUNK_SIZE - DEPTH_CHUNK_SIZE);
    }

    // 생성할 child의 path를 구한다.
    public CommentPath createChildCommentPath(String descendantTopPath) {
        if (descendantTopPath == null) {
            return CommentPath.create(path + MIN_CHUNK);
        }
        String childrenTopPath = findChildTopPath(descendantTopPath);
        return CommentPath.create(increase(childrenTopPath));
    }

    private String findChildTopPath(String descendantTopPath) {
        return descendantTopPath.substring(0, (getDepth() + 1) * DEPTH_CHUNK_SIZE);
    }

    private String increase(String path) {
        // 마지막 5개의 문자열 자름
        var lastChunk = path.substring(path.length() - DEPTH_CHUNK_SIZE);
        if (isChunkOverFlowed(lastChunk)) {
            throw new IllegalStateException("Chunk overflow");
        }
        int charsetLength = CHARSET.length();
        int value = 0;
        for (var ch: lastChunk.toCharArray()) {
            // 글자를 10진수로 변경함.
            value = value * charsetLength + CHARSET.indexOf(ch);
        }
        // 1 증가.
        value = value + 1;

        String result = "";
        // 다시 62 진수로 바꿈.
        for(int i = 0 ; i < DEPTH_CHUNK_SIZE; i++) {
            result = CHARSET.charAt(value % charsetLength) + result;
            value /= charsetLength;
        }

        return path.substring(0, path.length() - DEPTH_CHUNK_SIZE) + result;
    }

    private boolean isChunkOverFlowed(String lasChunk) {
        return MAX_CHUNK.equals(lasChunk);
    }
}

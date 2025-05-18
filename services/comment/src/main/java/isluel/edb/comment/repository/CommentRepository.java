package isluel.edb.comment.repository;

import isluel.edb.comment.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    @Query("select c from Comment c where c.commentPath.path = :path")
    Optional<Comment> findByPath(@Param("path") String path);

    // descendantTopPath
    @Query(
            value = " select path " +
                    " from comment_v2" +
                    " where article_id = :articleId and path > :pathPrefix and path like :pathPrefix%" +
                    " order by path desc limit 1",
            nativeQuery = true
    )
    Optional<String> findDescendantTopPath(
            @Param("articleId") Long articleId,
            @Param("pathPrefix") String pathPrefix
    );

    @Query(
            value = " select comment.comment_id, comment.content, comment.path, comment.article_id," +
                    "   comment.writer_id, comment.deleted, comment.created_at" +
                    " from (" +
                    "   select comment_id from comment where article_id = :articleId" +
                    "   order by path asc limit :limit offset :offset" +
                    " ) t left join comment on comment.comment_id = t.comment_id",
            nativeQuery = true
    )
    List<Comment> findAll(
            @Param("articleId") Long articleId,
            @Param("offset") Long offset,
            @Param("limit") Long limit
    );

    @Query(
            value = " select count(*) from (" +
                    "   select comment_id from comment where article_id = :articleId limit :limit" +
                    " ) t",
            nativeQuery = true
    )
    Long count(
            @Param("articleId") Long articleId,
            @Param("limit") Long limit
    );

    @Query(
            value = " select comment.comment_id, comment.content, comment.path, comment.article_id," +
                    "   comment.writer_id, comment.deleted, comment.created_at" +
                    " from comment" +
                    " where article_id = :articleId and path > :lastPath" +
                    " order by path asc limit :limit",
            nativeQuery = true
    )
    List<Comment> findAllInfiniteScroll(
            @Param("articleId") Long articleId,
            @Param("lastPath") String lastPath,
            @Param("limit") Long limit
    );

    @Query(
            value = " select comment.comment_id, comment.content, comment.path, comment.article_id," +
                    "   comment.writer_id, comment.deleted, comment.created_at" +
                    " from comment" +
                    " where article_id = :articleId" +
                    " order by path asc limit :limit",
            nativeQuery = true
    )
    List<Comment> findAllInfiniteScroll(
            @Param("articleId") Long articleId,
            @Param("limit") Long limit
    );
}

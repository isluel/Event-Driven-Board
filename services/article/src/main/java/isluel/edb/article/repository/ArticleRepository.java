package isluel.edb.article.repository;

import isluel.edb.article.entity.Article;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ArticleRepository extends JpaRepository<Article, Long> {

    @Query(
        value = "Select " +
                "   article.article_id, article.title, article.content, article.board_id, article.writer_id" +
                "   , article.created_at, article.modified_at " +
                "from " +
                " (select article_id from article where board_id = :boardId order by article_id desc limit :limit offset :offset) t " +
                " left join article on t.article_id = article.article_id",
            nativeQuery = true
    )
    List<Article> findAll(
            @Param("boardId") Long boardId,
            @Param("offset") Long offset,
            @Param("limit") Long limit
    );

    // 현제 페이지에서 표시되는 최대 페이지 개수만큼
    // 게시글이 조회가능한지 개수 카운트
    // 모두 검색 하지 않고 현제 페이지에서 표시될수있는 최대 페이지 수의 게시글 수만 확인
    @Query(
            value = " select count(*)" +
                    " from (select article_id from article where board_id = :boardId order by article_id limit :limit) t",
            nativeQuery = true
    )
    Long count(
            @Param("boardId") Long boardId,
            @Param("limit") Long limit
    );

    @Query(
            value = "Select " +
                    "   article.article_id, article.title, article.content, article.board_id, article.writer_id" +
                    "   , article.created_at, article.modified_at " +
                    " from article where board_id = :boardId order by article_id desc limit :limit",
            nativeQuery = true
    )
    List<Article> findAllInfiniteScroll(
            @Param("boardId") Long boardId,
            @Param("limit") Long limit
    );

    @Query(
            value = "Select " +
                    "   article.article_id, article.title, article.content, article.board_id, article.writer_id" +
                    "   , article.created_at, article.modified_at " +
                    " from article where board_id = :boardId and article_id < :lastArticleId order by article_id desc " +
                    " limit :limit",
            nativeQuery = true
    )
    List<Article> findAllInfiniteScroll(
            @Param("boardId") Long boardId,
            @Param("limit") Long limit,
            @Param("lastArticleId") Long lastArticleId
    );

}

package isluel.edb.view.repository;

import isluel.edb.view.entity.ArticleViewCount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ArticleViewCountBackUpRepository extends JpaRepository<ArticleViewCount, Long> {

    // 네트워크 지연 등으 문제로 낮은 viewCount가 나중에 도착하였을 경우 업데이트 하지 않도록 한다.
    @Query(
            value = " update article_view_count set view_count = :viewCount"+
                    " where article_id = :articleId and view_count < :viewCount",
            nativeQuery = true
    )
    @Modifying
    int updateViewCount(
            @Param("articleId") Long articleId,
            @Param("viewCount") Long viewCount
    );
}

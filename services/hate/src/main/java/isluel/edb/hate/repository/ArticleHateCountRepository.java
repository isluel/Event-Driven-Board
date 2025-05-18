package isluel.edb.hate.repository;

import isluel.edb.hate.entity.ArticleHateCount;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ArticleHateCountRepository extends JpaRepository<ArticleHateCount, Long> {

    // 비관적 락
    @Query(
            value = "update article_hate_count set hate_count = hate_count + 1 where article_id = :articleId",
            nativeQuery = true
    )
    @Modifying
    int increase(
            @Param("articleId")Long articleId
    );

    // 비관적 락
    @Query(
            value = "update article_hate_count set hate_count = hate_count - 1 where article_id = :articleId",
            nativeQuery = true
    )
    @Modifying
    int decrease(
            @Param("articleId")Long articleId
    );
}

package isluel.edb.hate.repository;

import isluel.edb.hate.entity.ArticleHate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ArticleHateRepository extends JpaRepository<ArticleHate, Long> {

    Optional<ArticleHate> findByArticleIdAndUserId(Long articleId, Long userId);
}

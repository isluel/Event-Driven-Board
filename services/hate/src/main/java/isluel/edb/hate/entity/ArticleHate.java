package isluel.edb.hate.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;

@Table(name = "article_hate")
@Entity
@Getter
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ArticleHate {

    @Id
    private Long articleHateId;
    private Long articleId;
    private Long userId;
    private LocalDateTime createdAt;

    public static ArticleHate create(Long articleHateId, Long articleId, Long userId) {
        ArticleHate articleHate = new ArticleHate();
        articleHate.articleHateId = articleHateId;
        articleHate.articleId = articleId;
        articleHate.userId = userId;
        articleHate.createdAt = LocalDateTime.now();
        return articleHate;
    }
}

package isluel.edb.hate.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Table(name = "article_hate_count")
@Getter
@ToString
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ArticleHateCount {

    @Id
    private Long articleId;
    private Long hateCount;

    public static ArticleHateCount init(Long articleId, Long hateCount) {
        ArticleHateCount articleHateCount = new ArticleHateCount();
        articleHateCount.articleId = articleId;
        articleHateCount.hateCount = hateCount;
        return articleHateCount;
    }

    public void increase() {
        this.hateCount++;
    }

    public void decrease() {
        this.hateCount--;
    }
}

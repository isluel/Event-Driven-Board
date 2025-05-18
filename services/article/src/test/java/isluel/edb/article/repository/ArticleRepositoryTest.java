package isluel.edb.article.repository;

import isluel.edb.article.entity.Article;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@Slf4j
@SpringBootTest
class ArticleRepositoryTest {

    @Autowired
    private ArticleRepository articleRepository;

    @Test
    void findAllTest() {
        List<Article> articles = articleRepository.findAll(1L, 149970L, 30L);
        log.info("article size: {}", articles.size());
        for (var article : articles) {
            log.info("article: {}", article);
        }

    }

    @Test
    void countTest() {
        Long count = articleRepository.count(1L, 10000L);
        log.info("count: {}", count);
    }

    @Test
    void findAllInfiniteScrollTest() {
        List<Article> articles = articleRepository.findAllInfiniteScroll(1L, 30L);
        for (var article : articles) {
            log.info("article: {}", article);
        }

        var lastArticleId = articles.getLast().getArticleId();
        log.info("lastArticleId: {}", lastArticleId);
        List<Article> articles2 = articleRepository.findAllInfiniteScroll(1L, 30L, lastArticleId);
        for (var article : articles2) {
            log.info("article: {}", article);
        }
    }
}
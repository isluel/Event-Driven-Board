package isluel.edb.hate.controller;

import isluel.edb.hate.service.ArticleHateService;
import isluel.edb.hate.service.response.ArticleHateResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class ArticleHateController {
    private final ArticleHateService articleHateService;

    @GetMapping("/v1/article-hates/articles/{articleId}/users/{userId}")
    public ArticleHateResponse hateArticle(
            @PathVariable("articleId") Long articleId,
            @PathVariable("userId") Long userId) {
        return articleHateService.read(articleId, userId);
    }

    @PostMapping("/v1/article-hates/articles/{articleId}/users/{userId}/hate")
    public void hate(
            @PathVariable("articleId") Long articleId,
            @PathVariable("userId") Long userId
    ) {
        articleHateService.hate(articleId, userId);
    }

    @DeleteMapping("/v1/article-hates/articles/{articleId}/users/{userId}/unhate")
    public void unhate(
            @PathVariable("articleId") Long articleId,
            @PathVariable("userId") Long userId
    ) {
        articleHateService.unHate(articleId, userId);
    }

    @GetMapping("/v1/article-hates/articles/{articleId}/count")
    public Long count(@PathVariable("articleId") Long articleId) {
        return articleHateService.count(articleId);
    }
}

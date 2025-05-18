package isluel.edb.hotarticle.controller;

import isluel.edb.hotarticle.service.HotArticleService;
import isluel.edb.hotarticle.service.response.HotArticleResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
public class HotArticleController {
    private final HotArticleService hotArticleService;

    @GetMapping("/v1/hot-articles/articles/date/{dateString}")
    public List<HotArticleResponse> getHotArticles(@PathVariable("dateString") String dateString) {
        return hotArticleService.readAll(dateString);
    }
}

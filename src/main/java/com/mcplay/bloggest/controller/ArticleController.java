package com.mcplay.bloggest.controller;

import com.mcplay.bloggest.exception.NotFoundException;
import com.mcplay.bloggest.model.Article;
import com.mcplay.bloggest.model.User;
import com.mcplay.bloggest.service.ArticleService;
import com.mcplay.bloggest.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@Controller
@RequestMapping("/article")
public class ArticleController {

    @Autowired
    private final ArticleService articleService;
    @Autowired
    private final UserService userService;

    public ArticleController(ArticleService articleService, UserService userService) {
        this.articleService = articleService;
        this.userService = userService;
    }

    @GetMapping
    public String index(Model model,
                        @AuthenticationPrincipal UserDetails userDetails,
                        @RequestParam(required = false, value = "q") String q,
                        @RequestParam(required = false, value = "page") Integer page,
                        @RequestParam(required = false, value = "size") Integer size) {
        if (q == null) {
            model.addAttribute("articles", articleService.getAll(getPageable(page, size)));
        } else {
            model.addAttribute("articles", articleService.search(q, getPageable(page, size)));
        }

        return "article/index";
    }

    @GetMapping("/show/{link}")
    public String getPost(@AuthenticationPrincipal UserDetails userDetails,
                          @PathVariable String link, Model model) {
        Optional<Article> article = articleService.getByLink(link);
        if (article.isPresent()) {
            model.addAttribute("article", article.get());
        } else {
            throwNotFoundException(link);
        }

        return "article/show";
    }

    @GetMapping("/new")
    public String newPost() {
        return "article/create";
    }

    @GetMapping("/edit/{id}")
    public String editPost(@AuthenticationPrincipal UserDetails userDetails, @PathVariable String id, Model model) {
        Optional<Article> article = articleService.getById(id);
        if (article.isPresent()) {
            model.addAttribute("article", article.get());
        } else {
            return throwNotFoundException(id);
        }

        return "article/create";
    }

    private String throwNotFoundException(@PathVariable String id) {
        throw new NotFoundException("Article Not Found for "+id);
    }

    @PostMapping("/delete/{id}")
    public String deletePost(@AuthenticationPrincipal UserDetails userDetails, @PathVariable String id, Model model) {
        articleService.deleteById(id);

        PageRequest pageRequest = PageRequest.of(0, 10, Sort.unsorted());
        model.addAttribute("message", "Article with id " + id + " deleted successfully!");
        model.addAttribute("articles", articleService.getAll(pageRequest));

        return "article/index";
    }

    @PostMapping
    public String savePost(@AuthenticationPrincipal UserDetails userDetails, Article article, Model model) {
        if (article.getId() == null || article.getId().length() == 0) {
            User user = userService.getByUsername(userDetails.getUsername());
            article.setAuthor(user);
        } else {
            Optional<Article> optionalArticle = articleService.getById(article.getId());
            if (optionalArticle.isPresent()) {
                article.setAuthor(optionalArticle.get().getAuthor());
            }
        }
        articleService.save(article);

        return "redirect:/article/show/"+article.getLink();
    }

    @GetMapping("/rest")
    @ResponseBody
    public Page<Article> articlesRest(@RequestParam(required = false, value = "page") Integer page,
                                      @RequestParam(required = false, value = "size") Integer size) {
        return articleService.getAll(getPageable(page, size));
    }

    private Pageable getPageable(Integer page, Integer size) {
        if (page == null || size == null) {
            return PageRequest.of(0, 20);
        }

        return PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdDate"));
    }
}

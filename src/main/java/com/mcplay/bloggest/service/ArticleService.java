package com.mcplay.bloggest.service;

import com.mcplay.bloggest.model.Article;
import com.mcplay.bloggest.repo.ArticleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
public class ArticleService {

    private final ArticleRepository articleRepository;

    public Article save(Article article) {
        if (article.getId() == null) {
            article.setId(UUID.randomUUID().toString());
        }
        return articleRepository.save(article);
    }

    public Page<Article> getAll(Pageable pageable) {
        return articleRepository.findAll(pageable);
    }

    public Optional<Article> getByLink(String link) {
        return articleRepository.findByLink(link);
    }

    public Optional<Article> getById(String id) {
        return articleRepository.findById(id);
    }

    public void deleteById(String id) {
        articleRepository.deleteById(id);
    }

    public Page<Article> search(String q, Pageable pageable) {
        return articleRepository.findByTitleContainingAndBodyContaining(q, q, pageable);
    }
}

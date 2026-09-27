package domain.service;

import domain.model.Article;
import domain.model.ArticleQuery;
import domain.repository.ArticleExporter;
import domain.repository.ArticleRepository;
import domain.validator.ArticleValidator;
import domain.validator.IdValidator;

import java.io.File;
import java.util.List;

public class ArticleService {
    private final ArticleRepository articleRepository;
    private final ArticleExporter articleExporter;
    private final ArticleValidator articleValidator;
    private final IdValidator idValidator;

    public ArticleService(
            ArticleRepository articleRepository,
            ArticleExporter articleExporter,
            ArticleValidator articleValidator,
            IdValidator idValidator
    ) {
        this.articleRepository = articleRepository;
        this.articleExporter = articleExporter;
        this.articleValidator = articleValidator;
        this.idValidator = idValidator;
    }

    public List<Article> getAll() {
        return articleRepository.getArticles();
    }

    public Article getById(int articleId) {
        idValidator.validate(articleId, "Article ID");
        return articleRepository.getArticleById(articleId);
    }

    public void add(Article article) {
        articleValidator.validate(article);
        articleRepository.addArticle(article);
    }

    public void edit(int articleId, String title, String content, Article.Status status) {
        idValidator.validate(articleId, "Article ID");

        Article article = articleRepository.getArticleById(articleId);

        article.setTitle(title);
        article.setContent(content);
        article.setStatus(status);

        articleValidator.validate(article);
        articleRepository.editArticle(article);
    }

    public void delete(int articleId) {
        idValidator.validate(articleId, "Article ID");
        articleRepository.deleteArticle(articleId);
    }

    public void exportToExcel(String filePath) {
        if (filePath == null || filePath.isBlank()) {
            throw new IllegalArgumentException("File path cannot be empty");
        }

        String targetPath = filePath.endsWith(".xlsx") ? filePath : filePath + ".xlsx";
        List<Article> articles = articleRepository.getArticles();

        if (articles.isEmpty()) {
            throw new IllegalStateException("No articles to export");
        }

        articleExporter.exportArticles(articles, new File(targetPath));
    }

    public List<Article> sortArticles(ArticleQuery.SortColumn col, ArticleQuery.SortDir dir) {
        return articleRepository.sortArticles(col, dir);
    }

}

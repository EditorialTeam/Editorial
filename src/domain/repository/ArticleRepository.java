package domain.repository;

import domain.model.Article;
import domain.model.ArticleQuery;

import java.util.List;

// Интерфейс для статей
public interface ArticleRepository {

    void addArticle(Article article);

    void deleteArticle(int articleId);

    void editArticle(Article article);

    void filterArticles();

    Article getArticleById(int articleId);

    List<Article> getArticles();

    List<Article> searchArticle(String keyword);

    List<Article> sortArticles(ArticleQuery.SortColumn sortCol, ArticleQuery.SortDir sortDir);
}

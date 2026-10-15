package presentation;

import domain.model.Article;
import domain.model.ArticleQuery;
import domain.model.EditorialStatistics;
import domain.model.User;
import domain.service.ArticleService;
import domain.service.StatisticsService;
import domain.service.UserService;

import java.util.List;

public class Presenter {
    private final ArticleService articleService;
    private final UserService userService;
    private final StatisticsService statisticsService;

    public Presenter(ArticleService articleService, UserService userService, StatisticsService statisticsService) {
        this.articleService = articleService;
        this.userService = userService;
        this.statisticsService = statisticsService;
    }

    public boolean onAddArticle(Article article) {
        articleService.add(article);
        return true;
    }

    public List<Article> onGetArticles() {
        return articleService.getAll();
    }

    public void onDeleteArticle(int articleId) {
        articleService.delete(articleId);
    }

    public Article onGetArticleById(int articleId) {
        return articleService.getById(articleId);
    }

    public List<Article> onFilterArticles(ArticleQuery.FilterColumn column, String value) {
        return articleService.filterArticles(column, value);
    }

    public List<Article> onSortArticles(ArticleQuery.SortColumn column, ArticleQuery.SortDir direction) {
        return articleService.sortArticles(column, direction);
    }

    public List<Article> onSearchArticle(String keyword) {
        return articleService.search(keyword);
    }

    public void onEditArticle(int articleId, String title, String content, Article.Status status) {
        articleService.edit(articleId, title, content, status);
    }

    public void onAddUser(User user) {
        userService.add(user);
    }

    public void onEditUser(int userId, String username, String email, String passwordHash, User.Role role) {
        userService.edit(userId, username, email, passwordHash, role);
    }

    public void onDeleteUser(int userId) {
        userService.delete(userId);
    }

    public User onGetUserById(int userId) {
        return userService.getById(userId);
    }

    public List<User> onGetUsers() {
        return userService.getAll();
    }

    public EditorialStatistics onGetStatistics() {
        return statisticsService.getStatistics();
    }

    public void onExportArticles(String filePath) {
        articleService.exportToExcel(filePath);
    }

    public void onExportUsers(String filePath) {
        userService.exportToExcel(filePath);
    }
}

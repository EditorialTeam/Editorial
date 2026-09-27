package presentation;

import domain.model.Article;
import domain.model.ArticleQuery;
import domain.model.EditorialStatistics;
import domain.model.User;
import domain.service.ArticleService;
import domain.service.StatisticsService;
import domain.service.UserService;

import java.util.List;

// Класс Presenter ("мозги" UI): связывает View и UseCase-ы бизнес-логики
public class Presenter {
    private final View view;
    private final ArticleService articleService;
    private final UserService userService;
    private final StatisticsService statisticsService;

    public Presenter(
            View view,
            ArticleService articleService,
            UserService userService,
            StatisticsService statisticsService
    ) {
        this.view = view;
        this.articleService = articleService;
        this.userService = userService;
        this.statisticsService = statisticsService;
    }

    // Обработка добавления статьи
    public boolean onAddArticle(Article article) {
        try {
            articleService.add(article);
            view.showMessage("Article added");
            return true;
        } catch (IllegalArgumentException | IllegalStateException e) {
            view.showError(e.getMessage());
            return false;
        }
    }

    // Обработка получения всех статей
    public List<Article> onGetArticles() {
        try {
            return articleService.getAll();
        } catch (IllegalStateException e) {
            view.showError(e.getMessage());
            return List.of();
        }
    }

    // Обработка удаления статьи по id
    public void onDeleteArticle(int articleId) {
        try {
            articleService.delete(articleId);
            view.showMessage("Article deleted");
        } catch (IllegalArgumentException | IllegalStateException e) {
            view.showError(e.getMessage());
        }
    }

    // Обработка получения статьи по id
    public Article onGetArticleById(int articleId) {
        try {
            return articleService.getById(articleId);
        } catch (IllegalArgumentException | IllegalStateException e) {
            view.showError(e.getMessage());
            return null;
        }
    }

    // Обработка фильтрации статей
    public void onFilterArticles() {
        // Filtering has not been implemented yet.
    }

    // Обработка сортировки статей
    public List<Article> onSortArticles(ArticleQuery.SortColumn col, ArticleQuery.SortDir dir) {
       try {
           return articleService.sortArticles(col, dir);
       } catch(IllegalArgumentException | IllegalStateException e) {
           view.showError(e.getMessage());
           return List.of();
       }
    }

    // Обработка поиска статей
    public void onSearchArticle() {
        // Searching has not been implemented yet.
    }

    // Обработка редактирования статьи по id
    public void onEditArticle(int articleId, String title, String content, Article.Status status) {
        try {
            articleService.edit(articleId, title, content, status);
            view.showMessage("Article edited");
        } catch (IllegalArgumentException | IllegalStateException e) {
            view.showError(e.getMessage());
        }
    }

    // Обработка добавления пользователя
    public void onAddUser(User user) {
        try {
            userService.add(user);
            view.showMessage("User added");
        } catch (IllegalArgumentException | IllegalStateException e) {
            view.showError(e.getMessage());
        }
    }

    // Обработка редактирования пользователя по id
    public void onEditUser(
            int userId,
            String username,
            String email,
            String passwordHash,
            User.Role role
    ) {
        try {
            userService.edit(userId, username, email, passwordHash, role);
            view.showMessage("User edited");
        } catch (IllegalArgumentException | IllegalStateException e) {
            view.showError(e.getMessage());
        }
    }

    // Обработка удаления пользователя по id
    public void onDeleteUser(int userId) {
        try {
            userService.delete(userId);
            view.showMessage("User deleted");
        } catch (IllegalArgumentException | IllegalStateException e) {
            view.showError(e.getMessage());
        }
    }

    // Обработка получения пользователя по id
    public User onGetUserById(int userId) {
        try {
            return userService.getById(userId);
        } catch (IllegalArgumentException | IllegalStateException e) {
            view.showError(e.getMessage());
            return null;
        }
    }

    public List<User> onGetUsers() {
        try {
            return userService.getAll();
        } catch (IllegalStateException e) {
            view.showError(e.getMessage());
            return List.of();
        }
    }

    public EditorialStatistics onGetStatistics() {
        try {
            return statisticsService.getStatistics();
        } catch (IllegalStateException e) {
            view.showError(e.getMessage());
            return EditorialStatistics.empty();
        }
    }

    public void onExportArticles(String filePath) {
        try {
            articleService.exportToExcel(filePath);
            view.showMessage("Articles exported successfully to " + filePath);
        } catch (IllegalArgumentException | IllegalStateException e) {
            view.showError(e.getMessage());
        }
    }
}

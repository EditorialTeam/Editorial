package presentation;

import domain.model.Article;
import domain.model.ArticleQuery;
import domain.model.EditorialStatistics;
import domain.model.User;
import domain.service.ArticleService;
import domain.service.StatisticsService;
import domain.service.UserService;

import java.io.File;
import java.util.List;
import java.util.function.Supplier;

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

    public Presenter(
            ArticleService articleService,
            UserService userService,
            StatisticsService statisticsService
    ) {
        this(null, articleService, userService, statisticsService);
    }

    public void executePresenterAction(Runnable action, String successMessage) {
        try {
            action.run();
            if (view != null) {
                view.showMessage(successMessage);
            }
        } catch (IllegalArgumentException | IllegalStateException e) {
            if (view != null) {
                view.showError(e.getMessage());
            }
        }
    }

    public <T> T executePresenterAction(Supplier<T> action, T fallback) {
        try {
            return action.get();
        } catch (IllegalArgumentException | IllegalStateException e) {
            if (view != null) {
                view.showError(e.getMessage());
            }
            return fallback;
        }
    }

    // Обработка добавления статьи
    public boolean onAddArticle(Article article) {
        return executePresenterAction(() -> {
            articleService.add(article);
            if (view != null) {
                view.showMessage("Article added");
            }
            return true;
        }, false);
    }

    // Обработка получения всех статей
    public List<Article> onGetArticles() {
        return executePresenterAction(articleService::getAll, List.of());
    }

    // Обработка удаления статьи по id
    public void onDeleteArticle(int articleId) {
        executePresenterAction(() -> articleService.delete(articleId), "Article deleted");
    }

    // Обработка получения статьи по id
    public Article onGetArticleById(int articleId) {
        return executePresenterAction((Supplier<Article>) () -> articleService.getById(articleId), null);
    }

    // Обработка фильтрации статей
    public List<Article> onFilterArticles(
            ArticleQuery.FilterColumn column,
            String value
    ) {
        return executePresenterAction(() -> articleService.filterArticles(column, value), List.of());
    }

    // Обработка сортировки статей
    public List<Article> onSortArticles(ArticleQuery.SortColumn col, ArticleQuery.SortDir dir) {
        return executePresenterAction(() -> articleService.sortArticles(col, dir), List.of());
    }

    // Обработка поиска статей
    public List<Article> onSearchArticle(String keyword) {
        return executePresenterAction(() -> articleService.search(keyword), List.of());
    }

    // Обработка редактирования статьи по id
    public void onEditArticle(int articleId, String title, String content, Article.Status status) {
        executePresenterAction(() -> articleService.edit(articleId, title, content, status), "Article edited");
    }

    // Обработка добавления пользователя
    public void onAddUser(User user) {
        executePresenterAction(() -> userService.add(user), "User added");
    }

    // Обработка редактирования пользователя по id
    public void onEditUser(
            int userId,
            String username,
            String email,
            String passwordHash,
            User.Role role
    ) {
        executePresenterAction(() -> userService.edit(userId, username, email, passwordHash, role), "User edited");
    }

    // Обработка удаления пользователя по id
    public void onDeleteUser(int userId) {
        executePresenterAction(() -> userService.delete(userId), "User deleted");
    }

    // Обработка получения пользователя по id
    public User onGetUserById(int userId) {
        return executePresenterAction((Supplier<User>) () -> userService.getById(userId), null);
    }

    public List<User> onGetUsers() {
        return executePresenterAction(userService::getAll, List.of());
    }

    public EditorialStatistics onGetStatistics() {
        return executePresenterAction(statisticsService::getStatistics, EditorialStatistics.empty());
    }

    public void onExportArticles(String filePath) {
        executePresenterAction(() -> {
            articleService.exportToExcel(filePath);
            File file = new File(filePath.endsWith(".xlsx") ? filePath : filePath + ".xlsx");
        }, "Articles exported successfully to " + filePath);
    }

    public void onExportUsers(String filePath) {
        executePresenterAction(() -> {
            userService.exportToExcel(filePath);
            File file = new File(filePath.endsWith(".xlsx") ? filePath : filePath + ".xlsx");
        }, "Users exported successfully to " + filePath);
    }
}

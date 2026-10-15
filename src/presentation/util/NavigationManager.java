package presentation.util;

import domain.model.Article;
import domain.service.ArticleService;
import domain.service.StatisticsService;
import domain.service.UserService;
import domain.validator.ArticleValidator;
import domain.validator.IdValidator;
import domain.validator.UserValidator;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import presentation.controller.*;
import presentation.controller.dialog.ArticleDialogController;
import presentation.validation.InputValidationService;
import presentation.Presenter;
import javafx.concurrent.Task;
import javafx.stage.FileChooser;

import java.io.IOException;
import java.io.File;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NavigationManager {
    private static NavigationManager instance;

    private Stage primaryStage;
    private StackPane contentArea;
    private MainController mainController;

    private ArticleService articleService;
    private UserService userService;
    private StatisticsService statisticsService;
    private InputValidationService inputValidationService;
    private Presenter presenter;
    private Runnable currentView = this::showCatalog;
    private final Set<Integer> deletingArticles = new HashSet<>();
    private boolean exporting;
    private final ExecutorService executor = Executors.newFixedThreadPool(3, action -> {
        Thread thread = new Thread(action, "editorial-worker");
        thread.setDaemon(true);
        return thread;
    });

    private NavigationManager() {}

    public static NavigationManager getInstance() {
        if (instance == null) {
            instance = new NavigationManager();
        }
        return instance;
    }

    public void init(Stage primaryStage, StackPane contentArea, MainController mainController,
                     ArticleService articleService, UserService userService,
                     StatisticsService statisticsService, InputValidationService inputValidationService) {
        this.primaryStage = primaryStage;
        this.contentArea = contentArea;
        this.mainController = mainController;
        this.articleService = articleService;
        this.userService = userService;
        this.statisticsService = statisticsService;
        this.inputValidationService = inputValidationService;
        this.presenter = new Presenter(articleService, userService, statisticsService);
    }

    public Presenter getPresenter() { return presenter; }

    public void execute(Runnable action) { executor.execute(action); }

    public void shutdown() { executor.shutdownNow(); }

    public void refreshCurrentView() { currentView.run(); }

    public void setArticleCount(int count) { mainController.setDataStatus(count); }

    public void setDatabaseError() { mainController.setDatabaseError(); }

    public void deleteArticle(Article article, Runnable onSuccess) {
        if (deletingArticles.contains(article.getId())) return;
        if (!DialogHelper.showConfirmation(primaryStage, "Удаление статьи",
                "Удалить «" + article.getTitle() + "»?", "Это действие нельзя отменить.")) return;
        deletingArticles.add(article.getId());
        Task<Void> task = new Task<>() {
            protected Void call() {
                presenter.onDeleteArticle(article.getId());
                return null;
            }
        };
        task.setOnSucceeded(event -> {
            deletingArticles.remove(article.getId());
            onSuccess.run();
        });
        task.setOnFailed(event -> {
            deletingArticles.remove(article.getId());
            DialogHelper.showError(primaryStage, "Ошибка удаления", task.getException().getMessage());
        });
        execute(task);
    }

    public void exportArticles() { export(false); }

    public void exportUsers() { export(true); }

    private void export(boolean users) {
        if (exporting) return;
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Сохранить Excel");
        chooser.setInitialFileName(users ? "users.xlsx" : "articles.xlsx");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel", "*.xlsx"));
        File file = chooser.showSaveDialog(primaryStage);
        if (file == null) return;
        String path = file.getAbsolutePath().endsWith(".xlsx") ? file.getAbsolutePath() : file.getAbsolutePath() + ".xlsx";
        exporting = true;
        Task<Void> task = new Task<>() {
            protected Void call() {
                if (users) presenter.onExportUsers(path);
                else presenter.onExportArticles(path);
                return null;
            }
        };
        task.setOnSucceeded(event -> {
            exporting = false;
            DialogHelper.showInfo(primaryStage, "Экспорт завершён", "Файл сохранён: " + path);
        });
        task.setOnFailed(event -> {
            exporting = false;
            DialogHelper.showError(primaryStage, "Ошибка экспорта", task.getException().getMessage());
        });
        execute(task);
    }

    public Stage getPrimaryStage() {
        return primaryStage;
    }

    public ArticleService getArticleService() {
        return articleService;
    }

    public UserService getUserService() {
        return userService;
    }

    public StatisticsService getStatisticsService() {
        return statisticsService;
    }

    public InputValidationService getInputValidationService() {
        return inputValidationService;
    }

    public void setView(Node node) {
        contentArea.getChildren().clear();
        contentArea.getChildren().add(node);
    }

    public void showCatalog() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/catalog-view.fxml"));
            Node view = loader.load();
            CatalogController controller = loader.getController();
            controller.init(this);
            setView(view);
            currentView = this::showCatalog;
            if (mainController != null) {
                mainController.setActiveTab("catalog");
            }
        } catch (IOException e) {
            e.printStackTrace();
            DialogHelper.showError(primaryStage, "Ошибка", "Не удалось загрузить каталог: " + e.getMessage());
        }
    }

    public void showTableView() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/table-view.fxml"));
            Node view = loader.load();
            ArticleTableController controller = loader.getController();
            controller.init(this);
            setView(view);
            currentView = this::showTableView;
            if (mainController != null) {
                mainController.setActiveTab("table");
            }
        } catch (IOException e) {
            e.printStackTrace();
            DialogHelper.showError(primaryStage, "Ошибка", "Не удалось загрузить таблицу статей: " + e.getMessage());
        }
    }

    public void showReader(Article article) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/reader-view.fxml"));
            Node view = loader.load();
            ReaderController controller = loader.getController();
            controller.init(this, article);
            setView(view);
            currentView = () -> {
                Task<Article> task = new Task<>() {
                    protected Article call() { return presenter.onGetArticleById(article.getId()); }
                };
                task.setOnSucceeded(event -> {
                    if (task.getValue() == null) showCatalog();
                    else showReader(task.getValue());
                });
                task.setOnFailed(event -> DialogHelper.showError(primaryStage, "Ошибка обновления", task.getException().getMessage()));
                execute(task);
            };
            if (mainController != null) {
                mainController.setActiveTab("reader");
            }
        } catch (IOException e) {
            e.printStackTrace();
            DialogHelper.showError(primaryStage, "Ошибка", "Не удалось открыть статью для чтения: " + e.getMessage());
        }
    }

    public void showStatistics() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/statistics-view.fxml"));
            Node view = loader.load();
            StatisticsController controller = loader.getController();
            controller.init(this);
            setView(view);
            currentView = this::showStatistics;
            if (mainController != null) {
                mainController.setActiveTab("stats");
            }
        } catch (IOException e) {
            e.printStackTrace();
            DialogHelper.showError(primaryStage, "Ошибка", "Не удалось загрузить аналитику: " + e.getMessage());
        }
    }

    public void showUsers() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/users-view.fxml"));
            Node view = loader.load();
            UsersController controller = loader.getController();
            controller.init(this);
            setView(view);
            currentView = this::showUsers;
            if (mainController != null) {
                mainController.setActiveTab("users");
            }
        } catch (IOException e) {
            e.printStackTrace();
            DialogHelper.showError(primaryStage, "Ошибка", "Не удалось загрузить пользователей: " + e.getMessage());
        }
    }

    public void openArticleDialog(Article article, Runnable onSaveSuccess) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/dialogs/article-dialog.fxml"));
            Parent root = loader.load();
            ArticleDialogController controller = loader.getController();

            Stage dialogStage = new Stage();
            dialogStage.setTitle(article == null ? "Новая статья — Редакция Вестник" : "Редактирование статьи — Редакция Вестник");
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.initOwner(primaryStage);

            Scene scene = new Scene(root);
            scene.getStylesheets().addAll(
                    getClass().getResource("/css/typography.css").toExternalForm(),
                    getClass().getResource("/css/editorial-theme.css").toExternalForm()
            );
            dialogStage.setScene(scene);
            dialogStage.setResizable(false);

            controller.init(this, dialogStage, article, onSaveSuccess);
            dialogStage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
            DialogHelper.showError(primaryStage, "Ошибка", "Не удалось открыть диалог статьи: " + e.getMessage());
        }
    }
}

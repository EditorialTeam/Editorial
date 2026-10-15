package application;

import data.local.database.DatabaseConfig;
import data.local.database.DatabaseConnectionFactory;
import data.local.database.DatabaseMigrator;
import data.local.exporter.PoiArticleExcelExporter;
import data.local.exporter.PoiUserExcelExporter;
import data.local.repository.JdbcArticleRepository;
import data.local.repository.JdbcUserRepository;
import domain.repository.ArticleExporter;
import domain.repository.ArticleRepository;
import domain.repository.UserExporter;
import domain.repository.UserRepository;
import domain.service.ArticleService;
import domain.service.StatisticsService;
import domain.service.UserService;
import domain.validator.ArticleValidator;
import domain.validator.IdValidator;
import domain.validator.UserValidator;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import presentation.controller.MainController;
import presentation.util.DialogHelper;
import presentation.util.NavigationManager;
import presentation.validation.InputValidationService;


public class ApplicationFX extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        DatabaseConfig config = new DatabaseConfig();
        DatabaseMigrator migrator = new DatabaseMigrator(config);
        DatabaseConnectionFactory connectionFactory = new DatabaseConnectionFactory(config);

        try {
            migrator.migrate();
        } catch (Exception e) {
            System.err.println("Предупреждение: миграция БД не выполнена (проверьте Docker PostgreSQL): " + e.getMessage());
        }

        ArticleRepository articleRepository = new JdbcArticleRepository(connectionFactory);
        UserRepository userRepository = new JdbcUserRepository(connectionFactory);

        ArticleExporter articleExporter = new PoiArticleExcelExporter();
        UserExporter userExporter = new PoiUserExcelExporter();

        ArticleValidator articleValidator = new ArticleValidator(userRepository);
        UserValidator userValidator = new UserValidator();
        IdValidator idValidator = new IdValidator();
        InputValidationService inputValidationService = new InputValidationService(
                idValidator,
                articleValidator,
                userValidator
        );

        ArticleService articleService = new ArticleService(
                articleRepository,
                articleExporter,
                articleValidator,
                idValidator
        );
        UserService userService = new UserService(userRepository, userExporter, userValidator, idValidator);
        StatisticsService statisticsService = new StatisticsService(articleRepository, userRepository);

        try {
            FXMLLoader mainLoader = new FXMLLoader(getClass().getResource("/view/main-layout.fxml"));
            Parent root = mainLoader.load();
            MainController mainController = mainLoader.getController();

            StackPane contentArea = (StackPane) root.lookup("#contentArea");

            NavigationManager navManager = NavigationManager.getInstance();
            navManager.init(primaryStage, contentArea, mainController,
                    articleService, userService, statisticsService, inputValidationService);
            mainController.init(navManager);

            navManager.showCatalog();

            Scene scene = new Scene(root, 1200, 780);
            scene.getStylesheets().addAll(
                    getClass().getResource("/css/typography.css").toExternalForm(),
                    getClass().getResource("/css/editorial-theme.css").toExternalForm()
            );

            primaryStage.setTitle("Редакция");
            primaryStage.setMinWidth(1024);
            primaryStage.setMinHeight(700);
            primaryStage.setScene(scene);
            mainController.installShortcuts();
            primaryStage.show();

        } catch (Exception e) {
            e.printStackTrace();
            DialogHelper.showError(primaryStage, "Критическая ошибка запуска",
                    "Не удалось инициализировать графический интерфейс JavaFX: " + e.getMessage());
        }
    }

    @Override
    public void stop() {
        NavigationManager.getInstance().shutdown();
    }

}

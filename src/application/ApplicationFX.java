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
import presentation.Presenter;
import presentation.validation.InputValidationService;
import javafx.application.Application;
import javafx.stage.Stage;

public class ApplicationFX extends Application {
    public static void main(String[] args) {
        launch(args);
    }

    public void start(Stage primaryStage) {
        // Подключение штук для БД
        DatabaseConfig config = new DatabaseConfig();
        DatabaseMigrator migrator = new DatabaseMigrator(config);
        DatabaseConnectionFactory connectionFactory = new DatabaseConnectionFactory(config);

        // Репозитории для доступа к данным через JDBC
        ArticleRepository articleRepository = new JdbcArticleRepository(connectionFactory);
        UserRepository userRepository = new JdbcUserRepository(connectionFactory);

        // Экспортер для экспорта Статей в Excel формат
        ArticleExporter articleExporter = new PoiArticleExcelExporter();
        UserExporter userExporter = new PoiUserExcelExporter();
        // Создание валидаторов
        ArticleValidator articleValidator = new ArticleValidator(userRepository);
        UserValidator userValidator = new UserValidator();
        IdValidator idValidator = new IdValidator();
        InputValidationService inputValidationService = new InputValidationService( // Подключение валидаторов
                idValidator, // в единый сервис inputValidationService
                articleValidator,
                userValidator
        );

        // Сервисы бизнес-логики
        ArticleService articleService = new ArticleService(
                articleRepository,
                articleExporter,
                articleValidator,
                idValidator
        );
        UserService userService = new UserService(userRepository, userExporter, userValidator, idValidator);
        StatisticsService statisticsService = new StatisticsService(articleRepository, userRepository);

        // UI// Ввод вывод текста в консоль
        Presenter presenter = new Presenter( // Обработка текста
                articleService,
                userService,
                statisticsService
        );

        // Запуск системы
        migrator.migrate();
    }
}

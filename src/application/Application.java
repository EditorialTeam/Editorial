package application;

import data.local.database.DatabaseConfig;
import data.local.database.DatabaseConnectionFactory;
import data.local.database.DatabaseMigrator;
import data.local.exporter.PoiArticleExcelExporter;
import data.local.repository.JdbcArticleRepository;
import data.local.repository.JdbcUserRepository;
import domain.repository.ArticleExporter;
import domain.repository.ArticleRepository;
import domain.repository.UserRepository;
import domain.service.ArticleService;
import domain.service.StatisticsService;
import domain.service.UserService;
import domain.validator.ArticleValidator;
import domain.validator.IdValidator;
import domain.validator.UserValidator;
import presentation.ConsoleView;
import presentation.Presenter;
import presentation.validation.InputValidationService;

public class Application {
    public static void main(String[] args) {
        // Подключение штук для БД
        DatabaseConfig config = new DatabaseConfig();
        DatabaseMigrator migrator = new DatabaseMigrator(config);
        DatabaseConnectionFactory connectionFactory = new DatabaseConnectionFactory(config);

        // Репозитории для доступа к данным через JDBC
        ArticleRepository articleRepository = new JdbcArticleRepository(connectionFactory);
        UserRepository userRepository = new JdbcUserRepository(connectionFactory);

        // Экспортер для экспорта Статей в Excel формат
        ArticleExporter articleExporter = new PoiArticleExcelExporter();
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
        UserService userService = new UserService(userRepository, userValidator, idValidator);
        StatisticsService statisticsService = new StatisticsService(articleRepository, userRepository);

        // UI
        ConsoleView view = new ConsoleView(inputValidationService); // Ввод вывод текста в консоль
        Presenter presenter = new Presenter( // Обработка текста
                view,
                articleService,
                userService,
                statisticsService
        );
        view.setPresenter(presenter);

        // Запуск системы
        migrator.migrate();
        view.run();
    }
}

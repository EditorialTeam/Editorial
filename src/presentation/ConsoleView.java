package presentation;

import domain.model.Article;
import domain.model.ArticleQuery;
import domain.model.EditorialStatistics;
import domain.model.User;
import presentation.validation.InputValidationService;

import java.util.List;
import java.util.Locale;
import java.util.Scanner;
import java.util.function.Consumer;
import java.util.function.IntConsumer;

public class ConsoleView implements View {
    // Константы - номера комманд

    // "Мозги" системы, сканер и сервис валидатора
    private Presenter presenter;
    private final Scanner scanner = new Scanner(System.in);
    private final InputValidationService inputValidationService;

    public ConsoleView(InputValidationService inputValidationService) {
        this.inputValidationService = inputValidationService;
    }

    public void setPresenter(Presenter presenter) {
        this.presenter = presenter;
    }

    public void run() {
        boolean isRunning = true; // флаг

        while (isRunning) {
            showStartOptions(); // Выводим стартовое UI-меню

            try {
                int command = getMenuChoice(); // Получаем комманду

                switch (command) {
                    case 1 -> showArticlesMenu();
                    case 2 -> showUsersMenu();
                    case 3 -> showStats();
                    case 4 -> exportMenu();
                    case 0 -> {
                        showMessage("Exiting the application");
                        isRunning = false;
                    }

                    default -> showError("Unknown command");
                }
            } catch (RuntimeException e) {
                showError(e.getMessage());
            }
        }
    }

    // UI - Вывод статьи (1-ой)
    @Override
    public void showArticle(Article article) {
        if (article == null) {
            return;
        }

        System.out.println();
        System.out.println("ID: " + article.getId());
        System.out.println("Author ID: " + article.getAuthorId());
        System.out.println("Title: " + article.getTitle());
        System.out.println("Content: " + article.getContent());
        System.out.println("Status: " + article.getStatus());
        System.out.println("Published at: " + article.getPublishedAt());
    }

    // UI - Вывод пользователя (1-го)
    @Override
    public void showUser(User user) {
        if (user == null) {
            return;
        }

        System.out.println();
        System.out.println("ID: " + user.getId());
        System.out.println("Username: " + user.getUsername());
        System.out.println("Email: " + user.getEmail());
        System.out.println("Role: " + user.getRole());
    }

    // UI - Добавления статьи по вводу из консоли
    private void addArticle() {
        int authorId = getValidatedIntInput("Enter author ID:", inputValidationService::validateAuthorId);
        String title = getValidatedInput("Enter title:", inputValidationService::validateArticleTitle);
        String content = getValidatedInput("Enter content:", inputValidationService::validateArticleContent);
        String publishedAt = getUserInput("Enter published at (leave empty if unpublished):").trim();

        if (publishedAt.isEmpty()) {
            publishedAt = null;
        }

        Article article = new Article(0, authorId, title, content, Article.Status.PENDING, publishedAt);
        presenter.onAddArticle(article);
    }

    // Метод удаления статьи по id
    private void deleteArticle() {
        int articleId = getPositiveIntInput("Enter article ID:", "Article ID");

        presenter.onDeleteArticle(articleId);
    }

    // Метод редактирования статьи по id: только статус или статья целиком
    private void editArticle() {
        int articleId = getPositiveIntInput("Enter article ID:", "Article ID");

        String title = getOptionalValidatedInput("Enter new title (if you want you can leave this empty - nothing will change):", inputValidationService::validateArticleTitle);
        String content = getOptionalValidatedInput("Enter new content (if you want you can leave this empty - nothing will change):", inputValidationService::validateArticleContent);
        Article.Status status = getStatusInput("Enter new status (if you want you can leave this empty - nothing will change):");

        if (title == null || content == null || status == null) {
            Article article = presenter.onGetArticleById(articleId);

            if (title == null) { title = article.getTitle(); }
            if (content == null) { content = article.getContent(); }
            if (status == null) { status = article.getStatus(); }
        }

        presenter.onEditArticle(articleId, title, content, status);
    }

    // Метод получения статьи по id
    private void getArticleById() {
        int articleId = getPositiveIntInput("Enter article ID to find:", "Article ID");

        Article article = presenter.onGetArticleById(articleId);

        showArticle(article);
    }

    private void filterArticles() {
        System.out.println("Filter articles by:");
        System.out.println("1. Publication date");
        System.out.println("2. Status");

        ArticleQuery.FilterColumn filterColumn;
        while (true) {
            int choice = getIntInput("Choose filter:");

            filterColumn = switch (choice) {
                case 1 -> ArticleQuery.FilterColumn.PUBLISHED_AT;
                case 2 -> ArticleQuery.FilterColumn.STATUS;
                default -> null;
            };

            if (filterColumn != null) {
                break;
            }

            showError("Choose a value from 1 to 2");
        }

        String filterValue;
        if (filterColumn == ArticleQuery.FilterColumn.PUBLISHED_AT) {
            filterValue = getValidatedInput(
                    "Enter publication date (YYYY-MM-DD):",
                    inputValidationService::validatePublicationDate
            );
        } else {
            filterValue = getValidatedStatusInput("Choose status:").name();
        }

        List<Article> articles = presenter.onFilterArticles(filterColumn, filterValue);

        if (articles.isEmpty()) {
            showMessage("No articles found");
            return;
        }

        for (Article article : articles) {
            showArticle(article);
        }
    }

    private void searchArticles() {
        String keyword = getUserInput("Enter keywords:");
        List<Article> articles = presenter.onSearchArticle(keyword);

        if (articles.isEmpty()) {
            showMessage("No articles found");
            return;
        }

        for (Article article : articles) {
            showArticle(article);
        }
    }

    private void sortArticles() {
        System.out.println("Sort articles by:");
        System.out.println("1. ID");
        System.out.println("2. Title");
        System.out.println("3. Published at");

        ArticleQuery.SortColumn sortColumn;
        while (true) {
            int columnChoice = getIntInput("Choose sort column:");

            sortColumn = switch (columnChoice) {
                case 1 -> ArticleQuery.SortColumn.ID;
                case 2 -> ArticleQuery.SortColumn.TITLE;
                case 3 -> ArticleQuery.SortColumn.PUBLISHED_AT;
                default -> null;
            };

            if (sortColumn != null) {
                break;
            }

            showError("Choose a value from 1 to 3");
        }

        System.out.println("Sort direction:");
        System.out.println("1. Ascending");
        System.out.println("2. Descending");

        ArticleQuery.SortDir sortDirection;
        while (true) {
            int directionChoice = getIntInput("Choose sort direction:");

            sortDirection = switch (directionChoice) {
                case 1 -> ArticleQuery.SortDir.ASC;
                case 2 -> ArticleQuery.SortDir.DESC;
                default -> null;
            };

            if (sortDirection != null) {
                break;
            }

            showError("Choose a value from 1 to 2");
        }

        List<Article> articles = presenter.onSortArticles(sortColumn, sortDirection);

        if (articles.isEmpty()) {
            showMessage("No articles found");
            return;
        }

        for (Article article : articles) {
            showArticle(article);
        }
    }
    // Метод добавления пользователя
    private void addUser() {
        String username = getValidatedInput("Enter username:", inputValidationService::validateUsername);
        String email = getValidatedInput("Enter email:", inputValidationService::validateEmail);
        String passwordHash = getValidatedInput("Enter password hash:", inputValidationService::validatePasswordHash);
        User.Role role = getRoleInput("Enter role:");

        User user = new User(0, username, email, passwordHash, role);
        presenter.onAddUser(user);
    }

    // Метод редактирования пользователя по id
    private void editUser() {
        int userId = getPositiveIntInput("Enter user ID:", "User ID");
        String username = getOptionalValidatedInput("Enter new username (if you want you can leave this empty - nothing will change):", inputValidationService::validateUsername);
        String email = getOptionalValidatedInput("Enter new email (if you want you can leave this empty - nothing will change):", inputValidationService::validateEmail);
        String passwordHash = getOptionalValidatedInput("Enter new password hash (if you want you can leave this empty - nothing will change):", inputValidationService::validatePasswordHash);
        User.Role role = getRoleInput("Enter new role (if you want you can leave this empty - nothing will change):");

        if (username == null || email == null || passwordHash == null || role == null) {
            User user = presenter.onGetUserById(userId);

            if (username == null) { username = user.getUsername(); }
            if (email == null) { email = user.getEmail(); }
            if (passwordHash == null) { passwordHash = user.getPasswordHash(); }
            if (role == null) { role = user.getRole(); }
        }

        presenter.onEditUser(userId, username, email, passwordHash, role);
    }

    // Метод удаления пользователя по id
    private void deleteUser() {
        int userId = getPositiveIntInput("Enter user ID:", "User ID");

        presenter.onDeleteUser(userId);
    }

    // Метод получения пользователя по id
    private void getUserById() {
        int userId = getPositiveIntInput("Enter user ID to find:", "User ID");
        User user = presenter.onGetUserById(userId);

        showUser(user);
    }

    // UI - стартовое меню
    @Override
    public void showStartOptions() {
        System.out.println("-------------------------");
        System.out.println("1. Articles menu");
        System.out.println("2. Users menu");
        System.out.println("3. Get stats");
        System.out.println("4. Export to Excel");
        System.out.println("0. Exit");
        System.out.println("-------------------------");
    }

    // UI - меню команд для статей
    private void showArticlesMenu(){
        boolean back = false;
        while (!back){
            System.out.println("--- ARTICLES MENU ---");
            System.out.println("1. Show all articles");
            System.out.println("2. Add article");
            System.out.println("3. Edit article");
            System.out.println("4. Delete article");
            System.out.println("5. Get article by ID");
            System.out.println("6. Filter articles");
            System.out.println("7. Sort articles");
            System.out.println("8. Search articles");
            System.out.println("0. Back to main menu");

            int command = getMenuChoice();
            switch (command){
                case 1 -> showArticles();
                case 2 -> addArticle();
                case 3 -> editArticle();
                case 4 -> deleteArticle();
                case 5 -> getArticleById();
                case 6 -> filterArticles();
                case 7 -> sortArticles();
                case 8 -> searchArticles();
                case 0 -> back = true;

                default -> showError("Unknown command!");
            }
        }
    }

    // UI - меню команд для юзера
    private void showUsersMenu(){
        boolean back = false;
        while (!back){
            System.out.println("--- USERS MENU ---");
            System.out.println("1. Add user");
            System.out.println("2. Edit user");
            System.out.println("3. Delete user");
            System.out.println("4. Get user by ID");
            System.out.println("5. Show all users");
            System.out.println("0. Back to main menu");

            int command = getMenuChoice();
            switch (command){
                case 1 -> addUser();
                case 2 -> editUser();
                case 3 -> deleteUser();
                case 4 -> getUserById();
                case 5 -> showUsers();
                case 0 -> back = true;

                default -> showError("Unknown command!");
            }
        }
    }


    // UI - вывод статей
    @Override
    public void showArticles() {
        List<Article> showArticlesList = presenter.onGetArticles();

        if (showArticlesList == null || showArticlesList.isEmpty()) {
            showMessage("No articles found");
            return;
        }

        for (Article article : showArticlesList) {
            showArticle(article);
        }
    }

    @Override
    public void showUsers() {
        List<User> users = presenter.onGetUsers();

        if (users.isEmpty()) {
            showMessage("No users found");
            return;
        }

        for (User user : users) {
            showUser(user);
        }
    }

    // Метод вывода сообщения
    @Override
    public void showMessage(String message) {
        System.out.println();
        System.out.println(message);
    }

    // Метод вывода ошибки
    @Override
    public void showError(String error) {
        System.out.println("Error: " + error);
    }

    // Метод получения ввода
    @Override
    public String getUserInput(String prompt) {
        System.out.println(prompt);
        System.out.flush();

        return scanner.nextLine();
    }

    // Метод для вывода строчки ввода комманды и соответственно получения команды
    @Override
    public int getMenuChoice() {
        return getIntInput("Enter command:");
    }

    // Метод получения числа из консоли
    private int getIntInput(String prompt) {
        while (true) {
            String input = getUserInput(prompt).trim();

            try {
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                showError("Enter a valid number");
            }
        }
    }

    // Метод получения положительного числа из консоли
    private int getPositiveIntInput(String prompt, String fieldName) {
        return getValidatedIntInput(prompt, value -> inputValidationService.validateId(value, fieldName));
    }

    // Метод получения валидированного числа ввода из консоли
    private int getValidatedIntInput(String prompt, IntConsumer validator) {
        while (true) {
            int value = getIntInput(prompt);
            try {
                validator.accept(value);
                return value;
            } catch (IllegalArgumentException e) {
                showError(e.getMessage());
            }
        }
    }

    // Метод получения валидированного ввода из консоли
    private String getValidatedInput(String prompt, Consumer<String> validator) {
        while (true) {
            String input = getUserInput(prompt).trim();

            try {
                validator.accept(input);
                return input;
            } catch (IllegalArgumentException e) {
                showError(e.getMessage());
            }
        }
    }

    private String getOptionalValidatedInput(String prompt, Consumer<String> validator) {
        while (true) {
            String input = getUserInput(prompt).trim();

            if (input.isEmpty()) {
                return null;
            }

            try {
                validator.accept(input);
                return input;
            } catch (IllegalArgumentException e) {
                showError(e.getMessage());
            }
        }
    }

    // Метод получения валидированного ввода статуса из консоли
    private Article.Status getStatusInput(String prompt) {
        while (true) {
            int num = 1;
            for (Article.Status item : Article.Status.values()){
                System.out.println(num + ". " + item);
                num += 1;
            }

            String input = getUserInput(prompt);
            if (input.isEmpty()) {
                return null;
            }

            try {
                int intInput = Integer.parseInt(input);
                return Article.Status.values()[intInput - 1];
            } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
                showError("Available statuses: PENDING, MODERATING, REJECTED, PUBLISHED");
            }
        }
    }

    private Article.Status getValidatedStatusInput(String prompt) {
        while (true) {
            Article.Status status = getStatusInput(prompt);

            try {
                inputValidationService.validateArticleStatus(status);
                return status;
            } catch (IllegalArgumentException e) {
                showError(e.getMessage());
            }
        }
    }

    // Метод получения валидированного ввода роли из консоли
    private User.Role getRoleInput(String prompt) {
        while (true) {
            int num = 1;
            for (User.Role item : User.Role.values()){
                System.out.println(num + ". " + item);
                num += 1;
            }

            String input = getUserInput(prompt);
            if (input.isEmpty()) {
                return null;
            }

            int intInput = Integer.parseInt(input);

            try {
                return User.Role.values()[intInput - 1];
            } catch (ArrayIndexOutOfBoundsException e){
                showError("Value out of bounds");
            }
        }
    }

    @Override
    public void showStats() {
        EditorialStatistics statistics = presenter.onGetStatistics();

        System.out.println("User count: " + statistics.getUserCount());
        System.out.println("Article count: " + statistics.getArticleCount());
        System.out.println("Articles awaiting moderation: " + statistics.getPendingArticleCount());
        System.out.println("Articles published: " + statistics.getPublishedArticleCount());
        System.out.println("Articles rejected: " + statistics.getRejectedArticleCount());
    }

    // Меню экспорта данных в Excel
    private void exportMenu() {
        boolean back = false;
        while (!back) {
            System.out.println("--- EXPORT DATA TO EXCEL ---");
            System.out.println("1. Export articles");
            System.out.println("2. Export users");
            System.out.println("0. Back to main menu");

            int command = getMenuChoice();
            switch (command) {
                case 1 -> exportArticles();
                case 2 -> exportUsers();
                case 0 -> back = true;
                default -> showError("Unknown command!");
            }
        }
    }

    // Отдельный метод экспорта статей
    private void exportArticles() {
        String path = askFilePathWithDefault("articles.xlsx");
        presenter.onExportArticles(path);
    }

    // Отдельный метод экспорта пользователей
    private void exportUsers() {
        String path = askFilePathWithDefault("users.xlsx");
        presenter.onExportUsers(path);
    }

    // Умный запрос пути: если юзер жмет Enter, подставляется имя по умолчанию
    private String askFilePathWithDefault(String defaultFileName) {
        System.out.println("Enter file path or name (press Enter to save as '" + defaultFileName + "'):");
        System.out.flush();
        String input = scanner.nextLine().trim();

        if (input.isEmpty()) {
            return defaultFileName;
        }
        return input;
    }

}

package presentation;

import domain.model.Article;
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
    private static final int SHOW_ARTICLES_COMMAND = 1;
    private static final int ADD_ARTICLE_COMMAND = 2;
    private static final int EDIT_ARTICLE_COMMAND = 3;
    private static final int DELETE_ARTICLE_COMMAND = 4;
    private static final int GET_ARTICLE_BY_ID_COMMAND = 5;
    private static final int FILTER_ARTICLES_COMMAND = 6;
    private static final int SORT_ARTICLES_COMMAND = 7;
    private static final int SEARCH_ARTICLES_COMMAND = 8;
    private static final int ADD_USER_COMMAND = 9;
    private static final int EDIT_USER_COMMAND = 10;
    private static final int DELETE_USER_COMMAND = 11;
    private static final int GET_USER_BY_ID_COMMAND = 12;
    private static final int SHOW_USERS_COMMAND = 13;
    private static final int SHOW_STATS = 14;
    private static final int EXPORT_ARTICLES_TO_EXCEL = 15;
    private static final int EXIT_COMMAND = 0;


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

                switch (command) { // В зависимости от комманды выбираем опцию
                    case SHOW_ARTICLES_COMMAND -> showArticles();
                    case ADD_ARTICLE_COMMAND -> addArticle();
                    case EDIT_ARTICLE_COMMAND -> editArticle();
                    case DELETE_ARTICLE_COMMAND -> deleteArticle();
                    case GET_ARTICLE_BY_ID_COMMAND -> getArticleById();
                    case FILTER_ARTICLES_COMMAND -> presenter.onFilterArticles();
                    case SORT_ARTICLES_COMMAND -> presenter.onSortArticles();
                    case SEARCH_ARTICLES_COMMAND -> presenter.onSearchArticle();
                    case ADD_USER_COMMAND -> addUser();
                    case EDIT_USER_COMMAND -> editUser();
                    case DELETE_USER_COMMAND -> deleteUser();
                    case GET_USER_BY_ID_COMMAND -> getUserById();
                    case SHOW_USERS_COMMAND -> showUsers();
                    case SHOW_STATS -> showStats();
                    case EXIT_COMMAND -> {
                        showMessage("Exiting the application");
                        isRunning = false;
                    }
                    case EXPORT_ARTICLES_TO_EXCEL -> exportArticles();
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

    // Метод редактирования статьи по id
    private void editArticle() {
        int articleId = getPositiveIntInput("Enter article ID:", "Article ID");
        String title = getValidatedInput("Enter new title:", inputValidationService::validateArticleTitle);
        String content = getValidatedInput("Enter new content:", inputValidationService::validateArticleContent);
        Article.Status status = getStatusInput("Enter new status:");

        presenter.onEditArticle(articleId, title, content, status);
    }

    // Метод получения статьи по id
    private void getArticleById() {
        int articleId = getPositiveIntInput("Enter article ID to find:", "Article ID");

        Article article = presenter.onGetArticleById(articleId);

        showArticle(article);
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
        String username = getValidatedInput("Enter new username:", inputValidationService::validateUsername);
        String email = getValidatedInput("Enter new email:", inputValidationService::validateEmail);
        String passwordHash = getValidatedInput("Enter new password hash:", inputValidationService::validatePasswordHash);
        User.Role role = getRoleInput("Enter new role:");

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
        System.out.println("1. Show all articles");
        System.out.println("2. Add article");
        System.out.println("3. Edit article");
        System.out.println("4. Delete article");
        System.out.println("5. Get article by ID");
        System.out.println("6. Filter articles");
        System.out.println("7. Sort articles");
        System.out.println("8. Search articles");
        System.out.println("9. Add user");
        System.out.println("10. Edit user");
        System.out.println("11. Delete user");
        System.out.println("12. Get user by ID");
        System.out.println("13. Show all users");
        System.out.println("14. Get stats");
        System.out.println("15. Export articles to Excel");
        System.out.println("0. Exit");
        System.out.println("-------------------------");
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

    // Метод получения валидированного ввода статуса из консоли
    private Article.Status getStatusInput(String prompt) {
        while (true) {
            String input = getUserInput(prompt).trim().toUpperCase(Locale.ROOT);

            try {
                return Article.Status.valueOf(input);
            } catch (IllegalArgumentException e) {
                showError("Available statuses: PENDING, MODERATING, REJECTED, PUBLISHED");
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

            int input = getIntInput(prompt);

            try {
                return User.Role.values()[input - 1];
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

    private void exportArticles(){
        String path = getUserInput("Enter file path (e. g. articles.xlsx)").trim();
        presenter.onExportArticles(path);

    }

}

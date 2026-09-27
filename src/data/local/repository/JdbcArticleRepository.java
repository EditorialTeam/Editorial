package data.local.repository;

import data.local.database.DatabaseConnectionFactory;
import domain.model.Article;
import domain.model.ArticleQuery;
import domain.repository.ArticleRepository;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;

// Класс для работы с БД в Java коде
public class JdbcArticleRepository implements ArticleRepository {
    private final DatabaseConnectionFactory connectionFactory;

    // Конструктор класса
    public JdbcArticleRepository(DatabaseConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    // Метод добавления статьи
    @Override
    public void addArticle(Article article) {
        // sql запрос
        String sql = """
                        INSERT INTO articles (author_id, status_id, title, content, published_at)
                        VALUES (?, (SELECT id FROM article_statuses WHERE code = ?), ?, ?, ?::timestamptz)
                        RETURNING id 
                """; // После вставки PostgreSQL возвращает сгенерированный ключ

        try (
                var connection = connectionFactory.openConnection(); // Открываем соединение
                var statement = connection.prepareStatement(sql); // Готовим запрос
        ) {
            // Задаем значения параметрам запроса
            statement.setInt(1, article.getAuthorId());
            statement.setString(2, article.getStatus().name());
            statement.setString(3, article.getTitle());
            statement.setString(4, article.getContent());
            statement.setString(5, article.getPublishedAt());

            // Пытаемся выполнить sql запрос
            try (var result = statement.executeQuery()) {
                if (!result.next()) throw new IllegalStateException("Database didn't return anything"); // Если не вернули id

                article.setId(result.getInt("id")); // Ставим id возвращенное PostgreSQL
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Couldn't add article", e); // Если возникла какая-нибудь ошибка при добавлении статьи возвращаем ошибку
        }
    }

    // Метод удаления статьи по id
    @Override
    public void deleteArticle(int articleId) {
        // sql запрос
        String sql = """
                DELETE FROM articles WHERE id = ?
                """;

        try (var connection = connectionFactory.openConnection(); // Открываем соединение
             var statement = connection.prepareStatement(sql); // Готовим запрос
        ) {
            // Задаем значения параметрам запроса
            statement.setInt(1, articleId);

            int affectedRows = statement.executeUpdate();

            if (affectedRows == 0) throw new IllegalArgumentException("No article found");


        } catch (SQLException e) {
            throw new IllegalStateException("Couldn't delete article");
        }
    }

    // Метод редактирования статьи по статье
    @Override
    public void editArticle(Article article) {
        // sql запрос: изменяем статью по id
        String sql = """
                UPDATE articles
                SET
                    status_id = (SELECT id FROM article_statuses WHERE code = ?),
                    title = ?,
                    content = ?,
                    published_at = ?::timestamptz
                WHERE id = ?
                """;

        try (var connection = connectionFactory.openConnection(); // Открываем соединение
             var statement = connection.prepareStatement(sql); // Готовим запрос
        ) {
            // Задаем значения параметрам запроса
            statement.setString(1, article.getStatus().name());
            statement.setString(2, article.getTitle());
            statement.setString(3, article.getContent());
            statement.setString(4, article.getPublishedAt());
            statement.setInt(5, article.getId());

            // Получаем кол-во измененных статей и выполняем запрос
            int affectedRows = statement.executeUpdate();

            if (affectedRows == 0) throw new IllegalArgumentException("Couldn't find article"); // Если не удалось изменить статью возвращаем ошибку

        } catch (SQLException e) {
            throw new IllegalStateException("Couldn't edit article"); // Если возникла какая-нибудь ошибка при изменении статьи возвращаем ошибку
        }
    }

    // Функция для фильтрации статей
    @Override
    public List<Article> filterArticles(
            ArticleQuery.FilterColumn filterColumn,
            String filterValue
    ) {
        if (filterColumn == null) {
            throw new IllegalArgumentException("Filter column is required");
        }
        if (filterValue == null || filterValue.isBlank()) {
            throw new IllegalArgumentException("Filter value is required");
        }

        String condition = switch (filterColumn) {
            case PUBLISHED_AT -> "article.published_at::date = ?::date";
            case STATUS -> "status.code = ?";
        };

        String parameter;
        try {
            parameter = switch (filterColumn) {
                case PUBLISHED_AT -> LocalDate.parse(filterValue.trim()).toString();
                case STATUS -> filterValue.trim().toUpperCase(Locale.ROOT);
            };
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Publication date must use YYYY-MM-DD format", e);
        }

        String sql = """
                SELECT
                    article.id,
                    article.author_id,
                    article.title,
                    article.content,
                    status.code AS status_code,
                    article.published_at
                FROM articles AS article
                JOIN article_statuses AS status ON status.id = article.status_id
                WHERE %s
                ORDER BY article.id ASC
                """.formatted(condition);

        try (var connection = connectionFactory.openConnection();
             var statement = connection.prepareStatement(sql)
        ) {
            statement.setString(1, parameter);

            try (var result = statement.executeQuery()) {
                List<Article> returnArticles = new ArrayList<Article>();

                while (result.next()) {
                    returnArticles.add(new Article(
                            result.getInt("id"),
                            result.getInt("author_id"),
                            result.getString("title"),
                            result.getString("content"),
                            Article.Status.valueOf(result.getString("status_code")),
                            result.getString("published_at")
                    ));
                }

                return returnArticles;
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Couldn't filter articles", e);
        }
    }

    // Функция для получения конкретной статьи по id
    @Override
    public Article getArticleById(int articleId) {
        // sql запрос: возвращаем данные о статье по id
        String sql = """
                SELECT
                    article.id,
                    article.author_id,
                    article.title,
                    article.content,
                    status.code AS status_code,
                    article.published_at
                FROM articles AS article
                JOIN article_statuses AS status ON status.id = article.status_id
                WHERE article.id = ?;
                """;

        try (var connection = connectionFactory.openConnection(); // Открываем соединение
             var statement = connection.prepareStatement(sql); // Готовим запрос
        ) {
            // Задаем значения параметрам запроса
            statement.setInt(1, articleId);

            // Получаем статью по id и выполняем запрос
            try (var result = statement.executeQuery()) {
                if (!result.next()) throw new IllegalArgumentException("Couldn't find an article"); // Если не удалось найти статью возвращаем ошибку

                // Создаем объект класса статьи в коде из полученных данных
                Article returnArticle = new Article(result.getInt("id"),
                        result.getInt("author_id"),
                        result.getString("title"),
                        result.getString("content"),
                        Article.Status.valueOf(result.getString("status_code")),
                        result.getString("published_at"));

                return returnArticle; // Возвращаем статью
            }

        } catch (SQLException e) {
            throw new IllegalStateException("Couldn't get the article by id"); // Если возникла какая-нибудь ошибка при поиске статьи возвращаем ошибку
        }

    }

    // Метод для получения статей
    @Override
    public List<Article> getArticles() {
        String sql = """
                SELECT
                    article.id,
                    article.author_id,
                    article.title,
                    article.content,
                    status.code AS status_code,
                    article.published_at
                FROM articles AS article
                JOIN article_statuses AS status ON status.id = article.status_id
                """;

        try (var connection = connectionFactory.openConnection();
             var statement = connection.prepareStatement(sql);
        ) {

            try (var resSet = statement.executeQuery()) {
                List<Article> returnArticles = new ArrayList<>();

                while (resSet.next()) {
                    returnArticles.add(new Article(
                            resSet.getInt("id"),
                            resSet.getInt("author_id"),
                            resSet.getString("title"),
                            resSet.getString("content"),
                            Article.Status.valueOf(resSet.getString("status_code")),
                            resSet.getString("published_at")
                    ));
                }

                return returnArticles;
            }

        }
        catch (SQLException e) {
            throw new IllegalStateException("Couldn't get articles", e);
        }

    }

    // Поиск статей по автору, названию и содержимому.
    // Полное совпадение слова выше, чем вхождение запроса внутрь слова.
    // Чем больше совпадений, тем выше статья. Знаки препинания не учитываются.
    @Override
    public List<Article> searchArticle(String keyword) {
        List<String> keywords = keywords(keyword);
        if (keywords.isEmpty()) {
            throw new IllegalArgumentException("Search query must contain a word");
        }

        String sql = """
                SELECT
                    article.id,
                    article.author_id,
                    author.username AS author_name,
                    article.title,
                    article.content,
                    status.code AS status_code,
                    article.published_at
                FROM articles AS article
                JOIN article_statuses AS status ON status.id = article.status_id
                JOIN users AS author ON author.id = article.author_id
                """;

        record ScoredArticle(Article article, int exactMatches, int partialMatches) {
        }

        try (var connection = connectionFactory.openConnection();
             var statement = connection.prepareStatement(sql)
        ) {
            try (var result = statement.executeQuery()) {
                List<ScoredArticle> scored = new ArrayList<>();

                while (result.next()) {
                    Article article = new Article(
                            result.getInt("id"),
                            result.getInt("author_id"),
                            result.getString("title"),
                            result.getString("content"),
                            Article.Status.valueOf(result.getString("status_code")),
                            result.getString("published_at")
                    );
                    int[] score = matchScore(
                            keywords,
                            result.getString("author_name"),
                            article.getTitle(),
                            article.getContent()
                    );
                    if (score[0] > 0 || score[1] > 0) {
                        scored.add(new ScoredArticle(article, score[0], score[1]));
                    }
                }

                scored.sort(Comparator
                        .comparingInt(ScoredArticle::exactMatches).reversed()
                        .thenComparing(Comparator.comparingInt(ScoredArticle::partialMatches).reversed())
                        .thenComparingInt(item -> item.article().getId()));

                return scored.stream().map(ScoredArticle::article).toList();
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Couldn't search articles", e);
        }
    }

    private static List<String> keywords(String query) {
        return new ArrayList<>(new LinkedHashSet<>(tokenize(query)));
    }

    private static int[] matchScore(List<String> keywords, String authorName, String title, String content) {
        List<String> tokens = new ArrayList<>();
        tokens.addAll(tokenize(authorName));
        tokens.addAll(tokenize(title));
        tokens.addAll(tokenize(content));

        int exactMatches = 0;
        int partialMatches = 0;
        for (String keyword : keywords) {
            for (String token : tokens) {
                if (token.equals(keyword)) {
                    exactMatches++;
                } else if (token.contains(keyword)) {
                    partialMatches++;
                }
            }
        }
        return new int[]{exactMatches, partialMatches};
    }

    private static List<String> tokenize(String text) {
        if (text == null || text.isBlank()) {
            return List.of();
        }

        String normalized = text.toLowerCase(Locale.ROOT).replaceAll("\\p{P}+", " ").trim();
        if (normalized.isEmpty()) {
            return List.of();
        }

        return Arrays.stream(normalized.split("\\s+"))
                .filter(token -> !token.isEmpty())
                .toList();
    }

    // Метод для сортировки нескольких статей по чему-то (по факту поиск + сортировка после полученных статей)
    @Override
    public List<Article> sortArticles(ArticleQuery.SortColumn sortCol, ArticleQuery.SortDir sortDir) {
        if (sortCol == null) {
            throw new IllegalArgumentException("Sort column is required");
        }
        if (sortDir == null) {
            throw new IllegalArgumentException("Sort direction is required");
        }

        String column = switch(sortCol) {
            case ID -> "article.id";
            case TITLE -> "article.title";
            case PUBLISHED_AT -> "article.published_at";
        };

        String dir = switch(sortDir) {
            case ASC -> "ASC";
            case DESC -> "DESC";
        };

        String sql = """
                SELECT
                    article.id,
                    article.author_id,
                    article.title,
                    article.content,
                    status.code AS status_code,
                    article.published_at
                FROM articles AS article
                JOIN article_statuses AS status ON status.id = article.status_id
                ORDER BY %s %s NULLS LAST, article.id ASC
                """.formatted(column, dir);

        try (var connection = connectionFactory.openConnection();
             var statement = connection.prepareStatement(sql)
        ) {
            try (var resSet = statement.executeQuery()) {
                List<Article> returnArticles = new ArrayList<>();

                while (resSet.next()) {
                    returnArticles.add(new Article(
                            resSet.getInt("id"),
                            resSet.getInt("author_id"),
                            resSet.getString("title"),
                            resSet.getString("content"),
                            Article.Status.valueOf(resSet.getString("status_code")),
                            resSet.getString("published_at")
                    ));
                }

                return returnArticles;
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Couldn't sort articles", e);
        }
    }
}

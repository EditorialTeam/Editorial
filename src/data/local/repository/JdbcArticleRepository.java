package data.local.repository;

import com.fasterxml.jackson.databind.ext.SqlBlobSerializer;
import data.local.database.DatabaseConnectionFactory;
import domain.model.Article;
import domain.model.ArticleQuery;
import domain.repository.ArticleRepository;
import org.apache.poi.sl.draw.geom.ArcToCommandIf;

import javax.xml.crypto.Data;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

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
                    content = ?
                WHERE id = ?
                """;

        try (var connection = connectionFactory.openConnection(); // Открываем соединение
             var statement = connection.prepareStatement(sql); // Готовим запрос
        ) {
            // Задаем значения параметрам запроса
            statement.setString(1, article.getStatus().name());
            statement.setString(2, article.getTitle());
            statement.setString(3, article.getContent());
            statement.setInt(4, article.getId());

            // Получаем кол-во измененных статей и выполняем запрос
            int affectedRows = statement.executeUpdate();

            if (affectedRows == 0) throw new IllegalArgumentException("Couldn't find article"); // Если не удалось изменить статью возвращаем ошибку

        } catch (SQLException e) {
            throw new IllegalStateException("Couldn't edit article"); // Если возникла какая-нибудь ошибка при изменении статьи возвращаем ошибку
        }
    }

    // Функция для фильтрации статей
    @Override
    public void filterArticles() {

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
                SELECT * FROM articles;
                """;

        try (var connection = connectionFactory.openConnection();
             var statement = connection.prepareStatement(sql);
        ) {

            try (var resSet = statement.executeQuery()) {
                List<Article> returnArticles = new ArrayList<Article>();

                while (resSet.next()) {
                    returnArticles.add(getArticleById(resSet.getInt("id")));
                }

                return returnArticles;
            }

        }
        catch (SQLException e) {
            throw new IllegalStateException("Couldn't get articles", e);
        }

    }

    // Метод для поиска нескольких статей по чему-то
    @Override
    public List<Article> searchArticle(String keyword) {
        return List.of();
    }

    // Метод для сортировки нескольких статей по чему-то (по факту поиск + сортировка после полученных статей)
    @Override
    public List<Article> sortArticles(ArticleQuery.SortColumn sortCol, ArticleQuery.SortDir sortDir) {
        String column = switch(sortCol) {
            case ID -> "id";
            case TITLE -> "title";
            case PUBLISHED_AT -> "published_at";
        };

        String dir = switch(sortDir) {
            case ASC -> "ASC";
            case DESC -> "DESC";
        };

        String sql = "SELECT * FROM articles ORDER BY " + column + " " + dir;

        try (var connection = connectionFactory.openConnection();
             var statement = connection.prepareStatement(sql);
        ) {
            try (var resSet = statement.executeQuery()) {
                List<Article> returnArticles = new ArrayList<Article>();
                while (resSet.next()) {
                    returnArticles.add(getArticleById(resSet.getInt("id")));
                }

                if (returnArticles.isEmpty()) throw new IllegalStateException("No articles to sort");

                return returnArticles;
            }
        }
        catch (SQLException e) {
            throw new IllegalStateException("Couldn't sort articles", e);
        }

    }
}

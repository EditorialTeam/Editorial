package domain.validator;

import domain.model.Article;
import domain.repository.UserRepository;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

public class ArticleValidator {
    // Минимальные/Максимальные константы
    private static final int TITLE_MIN_LENGTH = 3;
    private static final int TITLE_MAX_LENGTH = 255;
    private static final int CONTENT_MIN_LENGTH = 10;
    private static final int CONTENT_MAX_LENGTH = 10000;

    private final UserRepository userRepository;

    public ArticleValidator(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public void validate(Article article) {
        // Проверка существования репозитория
        if (article == null) {
            throw new IllegalArgumentException("Article is required");
        }

        // Валидируем все столбцы
        validateAuthorId(article.getAuthorId());
        validateTitle(article.getTitle());
        validateContent(article.getContent());
        validateStatus(article.getStatus());
        if (article.getStatus() == Article.Status.PUBLISHED && isBlank(article.getPublishedAt())) {
            throw new IllegalArgumentException("Published article must have a publication date"); // Если статья опубликована, но при этом не имеет даты публикации - возвращается ошибка
        }
    }

    // Метод валидации Id автора (>0 & exist)
    public void validateAuthorId(int authorId) {
        if (authorId <= 0) {
            throw new IllegalArgumentException("Author ID must be a positive number");
        }
        if (!userRepository.existsById(authorId)) {
            throw new IllegalArgumentException("Author with ID " + authorId + " does not exist");
        }
    }

    // Метод валидации заголовка статьи (!null & удовл min и max длине)
    public void validateTitle(String title) {
        if (isBlank(title)) {
            throw new IllegalArgumentException("Title cannot be empty");
        }
        if (title.length() < TITLE_MIN_LENGTH) {
            throw new IllegalArgumentException("Title must be at least " + TITLE_MIN_LENGTH + " characters");
        }
        if (title.length() > TITLE_MAX_LENGTH) {
            throw new IllegalArgumentException("Title must be at most " + TITLE_MAX_LENGTH + " characters");
        }
    }

    // Метод валидации контента статьи (!null & удовл min и max длине)
    public void validateContent(String content) {
        if (isBlank(content)) {
            throw new IllegalArgumentException("Content cannot be empty");
        }
        if (content.length() < CONTENT_MIN_LENGTH) {
            throw new IllegalArgumentException("Content must be at least " + CONTENT_MIN_LENGTH + " characters");
        }
        if (content.length() > CONTENT_MAX_LENGTH) {
            throw new IllegalArgumentException("Content must be at most " + CONTENT_MAX_LENGTH + " characters");
        }
    }

    // Метод валидации статуса статьи (!null)
    public void validateStatus(Article.Status status) {
        if (status == null) {
            throw new IllegalArgumentException("Status is required");
        }
    }

    public void validatePublicationDate(String publicationDate) {
        if (isBlank(publicationDate)) {
            throw new IllegalArgumentException("Publication date is required");
        }

        try {
            LocalDate.parse(publicationDate);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Publication date must use YYYY-MM-DD format", e);
        }
    }

    // Метод проверки на пустое значение
    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}

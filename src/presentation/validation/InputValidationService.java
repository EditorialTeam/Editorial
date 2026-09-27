package presentation.validation;

import domain.model.Article;
import domain.validator.ArticleValidator;
import domain.validator.IdValidator;
import domain.validator.UserValidator;

// Сервис содеражащий в себе все валидаторы и их функции
public class InputValidationService {
    private final IdValidator idValidator;
    private final ArticleValidator articleValidator;
    private final UserValidator userValidator;

    public InputValidationService(
            IdValidator idValidator,
            ArticleValidator articleValidator,
            UserValidator userValidator
    ) {
        this.idValidator = idValidator;
        this.articleValidator = articleValidator;
        this.userValidator = userValidator;
    }

    public void validateId(int id, String fieldName) {
        idValidator.validate(id, fieldName);
    }

    public void validateAuthorId(int authorId) {
        articleValidator.validateAuthorId(authorId);
    }

    public void validateArticleTitle(String title) {
        articleValidator.validateTitle(title);
    }

    public void validateArticleContent(String content) {
        articleValidator.validateContent(content);
    }

    public void validateArticleStatus(Article.Status status) {
        articleValidator.validateStatus(status);
    }

    public void validatePublicationDate(String publicationDate) {
        articleValidator.validatePublicationDate(publicationDate);
    }

    public void validateUsername(String username) {
        userValidator.validateUsername(username);
    }

    public void validateEmail(String email) {
        userValidator.validateEmail(email);
    }

    public void validatePasswordHash(String passwordHash) {
        userValidator.validatePasswordHash(passwordHash);
    }
}

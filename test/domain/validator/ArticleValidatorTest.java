package domain.validator;

import domain.model.Article;
import domain.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ArticleValidatorTest {
    @Mock
    private UserRepository userRepository;

    private ArticleValidator validator;

    @BeforeEach
    void setUp() {
        validator = new ArticleValidator(userRepository);
    }

    @Test
    void acceptsValidArticleWhenAuthorExists() {
        when(userRepository.existsById(7)).thenReturn(true);

        assertDoesNotThrow(() -> validator.validate(validArticle(Article.Status.PENDING, null)));
        verify(userRepository).existsById(7);
    }

    @Test
    void rejectsNullArticle() {
        assertEquals("Article is required", exceptionMessage(() -> validator.validate(null)));
    }

    @Test
    void rejectsNonPositiveAuthorIdWithoutLookingUpUser() {
        assertEquals(
                "Author ID must be a positive number",
                exceptionMessage(() -> validator.validateAuthorId(0))
        );
        verify(userRepository, never()).existsById(0);
    }

    @Test
    void rejectsMissingAuthor() {
        when(userRepository.existsById(7)).thenReturn(false);

        assertEquals(
                "Author with ID 7 does not exist",
                exceptionMessage(() -> validator.validateAuthorId(7))
        );
    }

    @Test
    void rejectsInvalidTitle() {
        assertEquals("Title cannot be empty", exceptionMessage(() -> validator.validateTitle(" ")));
        assertEquals(
                "Title must be at least 3 characters",
                exceptionMessage(() -> validator.validateTitle("ab"))
        );
        assertEquals(
                "Title must be at most 255 characters",
                exceptionMessage(() -> validator.validateTitle("a".repeat(256)))
        );
    }

    @Test
    void rejectsInvalidContent() {
        assertEquals("Content cannot be empty", exceptionMessage(() -> validator.validateContent("")));
        assertEquals(
                "Content must be at least 10 characters",
                exceptionMessage(() -> validator.validateContent("too short"))
        );
        assertEquals(
                "Content must be at most 10000 characters",
                exceptionMessage(() -> validator.validateContent("a".repeat(10001)))
        );
    }

    @Test
    void rejectsMissingStatus() {
        assertEquals("Status is required", exceptionMessage(() -> validator.validateStatus(null)));
    }

    @Test
    void rejectsPublishedArticleWithoutDate() {
        when(userRepository.existsById(7)).thenReturn(true);

        assertEquals(
                "Published article must have a publication date",
                exceptionMessage(() -> validator.validate(validArticle(Article.Status.PUBLISHED, null)))
        );
    }

    @Test
    void acceptsPublishedArticleWithDate() {
        when(userRepository.existsById(7)).thenReturn(true);

        assertDoesNotThrow(() -> validator.validate(validArticle(Article.Status.PUBLISHED, "2026-09-26")));
    }

    @Test
    void validatesPublicationDateFormat() {
        assertDoesNotThrow(() -> validator.validatePublicationDate("2026-09-27"));
        assertEquals(
                "Publication date must use YYYY-MM-DD format",
                exceptionMessage(() -> validator.validatePublicationDate("27.09.2026"))
        );
    }

    private static Article validArticle(Article.Status status, String publishedAt) {
        return new Article(1, 7, "Title", "Long enough", status, publishedAt);
    }

    private static String exceptionMessage(Runnable action) {
        return assertThrows(IllegalArgumentException.class, action::run).getMessage();
    }
}

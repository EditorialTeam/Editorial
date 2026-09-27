package domain.service;

import domain.model.Article;
import domain.repository.ArticleExporter;
import domain.repository.ArticleRepository;
import domain.repository.UserRepository;
import domain.validator.ArticleValidator;
import domain.validator.IdValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ArticleServiceEditTest {
    @Mock
    private ArticleRepository articleRepository;
    @Mock
    private ArticleExporter articleExporter;
    @Mock
    private UserRepository userRepository;

    private ArticleService articleService;

    @BeforeEach
    void setUp() {
        articleService = new ArticleService(
                articleRepository,
                articleExporter,
                new ArticleValidator(userRepository),
                new IdValidator()
        );
    }

    @Test
    void editUpdatesTitleContentAndStatus() {
        Article article = pendingArticle();
        when(userRepository.existsById(7)).thenReturn(true);
        when(articleRepository.getArticleById(1)).thenReturn(article);

        articleService.edit(1, "New title", "Updated content", Article.Status.MODERATING);

        assertEquals("New title", article.getTitle());
        assertEquals("Updated content", article.getContent());
        assertEquals(Article.Status.MODERATING, article.getStatus());
        verify(articleRepository).editArticle(article);
    }

    @Test
    void editChangesOnlyStatusWhenTitleAndContentStayTheSame() {
        Article article = pendingArticle();
        when(userRepository.existsById(7)).thenReturn(true);
        when(articleRepository.getArticleById(1)).thenReturn(article);

        articleService.edit(1, "Title", "Long enough", Article.Status.MODERATING);

        assertEquals(Article.Status.MODERATING, article.getStatus());
        assertEquals("Title", article.getTitle());
        assertEquals("Long enough", article.getContent());
        verify(articleRepository).editArticle(article);
    }

    @Test
    void editAllowsPublishedWhenDateIsPresent() {
        Article article = new Article(1, 7, "Title", "Long enough", Article.Status.PENDING, "2026-09-26");
        when(userRepository.existsById(7)).thenReturn(true);
        when(articleRepository.getArticleById(1)).thenReturn(article);

        articleService.edit(1, "Title", "Long enough", Article.Status.PUBLISHED);

        assertEquals(Article.Status.PUBLISHED, article.getStatus());
        verify(articleRepository).editArticle(article);
    }

    @Test
    void editRejectsInvalidArticleId() {
        assertThrows(
                IllegalArgumentException.class,
                () -> articleService.edit(0, "Title", "Long enough", Article.Status.REJECTED)
        );
        verify(articleRepository, never()).getArticleById(0);
        verify(articleRepository, never()).editArticle(any());
    }

    @Test
    void editRejectsNullStatus() {
        when(userRepository.existsById(7)).thenReturn(true);
        when(articleRepository.getArticleById(1)).thenReturn(pendingArticle());

        IllegalArgumentException error = assertThrows(
                IllegalArgumentException.class,
                () -> articleService.edit(1, "Title", "Long enough", null)
        );

        assertEquals("Status is required", error.getMessage());
        verify(articleRepository, never()).editArticle(any());
    }

    @Test
    void editRejectsPublishedArticleWithoutDate() {
        when(userRepository.existsById(7)).thenReturn(true);
        when(articleRepository.getArticleById(1)).thenReturn(pendingArticle());

        IllegalArgumentException error = assertThrows(
                IllegalArgumentException.class,
                () -> articleService.edit(1, "Title", "Long enough", Article.Status.PUBLISHED)
        );

        assertEquals("Published article must have a publication date", error.getMessage());
        verify(articleRepository, never()).editArticle(any());
    }

    private static Article pendingArticle() {
        return new Article(1, 7, "Title", "Long enough", Article.Status.PENDING, null);
    }
}

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
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ArticleServiceTest {
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
    void addPersistsValidArticle() {
        when(userRepository.existsById(7)).thenReturn(true);
        Article article = article();

        articleService.add(article);

        verify(articleRepository).addArticle(article);
    }

    @Test
    void addDoesNotPersistWhenAuthorIsMissing() {
        when(userRepository.existsById(7)).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () -> articleService.add(article()));
        verify(articleRepository, never()).addArticle(any());
    }

    @Test
    void editUpdatesFieldsAndSaves() {
        when(userRepository.existsById(7)).thenReturn(true);
        Article existing = article();
        when(articleRepository.getArticleById(1)).thenReturn(existing);

        articleService.edit(1, "New title", "Updated content", Article.Status.MODERATING);

        assertEquals("New title", existing.getTitle());
        assertEquals("Updated content", existing.getContent());
        assertEquals(Article.Status.MODERATING, existing.getStatus());
        verify(articleRepository).editArticle(existing);
    }

    @Test
    void editRejectsInvalidId() {
        assertThrows(
                IllegalArgumentException.class,
                () -> articleService.edit(0, "New title", "Updated content", Article.Status.PENDING)
        );
        verify(articleRepository, never()).getArticleById(0);
        verify(articleRepository, never()).editArticle(any());
    }

    @Test
    void editDoesNotSaveWhenValidationFails() {
        when(userRepository.existsById(7)).thenReturn(true);
        when(articleRepository.getArticleById(1)).thenReturn(article());

        assertThrows(
                IllegalArgumentException.class,
                () -> articleService.edit(1, "ok", "short", Article.Status.PENDING)
        );
        verify(articleRepository, never()).editArticle(any());
    }

    @Test
    void getByIdReturnsRepositoryResult() {
        Article existing = article();
        when(articleRepository.getArticleById(1)).thenReturn(existing);

        Article result = articleService.getById(1);

        assertSame(existing, result);
    }

    @Test
    void getAllReturnsRepositoryResult() {
        List<Article> articles = List.of(article());
        when(articleRepository.getArticles()).thenReturn(articles);

        assertSame(articles, articleService.getAll());
    }

    @Test
    void deleteDeletesAfterIdCheck() {
        articleService.delete(4);

        verify(articleRepository).deleteArticle(4);
    }

    @Test
    void getAndDeleteRejectInvalidId() {
        assertThrows(IllegalArgumentException.class, () -> articleService.getById(-1));
        assertThrows(IllegalArgumentException.class, () -> articleService.delete(0));
        verify(articleRepository, never()).getArticleById(-1);
        verify(articleRepository, never()).deleteArticle(0);
    }

    @Test
    void exportAddsXlsxExtensionAndDelegatesToExporter() {
        List<Article> articles = List.of(article());
        when(articleRepository.getArticles()).thenReturn(articles);

        articleService.exportToExcel("articles");

        ArgumentCaptor<File> fileCaptor = ArgumentCaptor.forClass(File.class);
        verify(articleExporter).exportArticles(any(), fileCaptor.capture());
        assertEquals("articles.xlsx", fileCaptor.getValue().getPath());
    }

    @Test
    void exportRejectsBlankPathAndEmptyArticleList() {
        assertThrows(IllegalArgumentException.class, () -> articleService.exportToExcel(" "));

        when(articleRepository.getArticles()).thenReturn(List.of());
        assertThrows(IllegalStateException.class, () -> articleService.exportToExcel("articles.xlsx"));
        verify(articleExporter, never()).exportArticles(any(), any());
    }

    private static Article article() {
        return new Article(1, 7, "Title", "Long enough", Article.Status.PENDING, null);
    }
}

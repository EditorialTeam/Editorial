package presentation;

import domain.model.Article;
import domain.model.ArticleQuery;
import domain.model.User;
import domain.service.ArticleService;
import domain.service.StatisticsService;
import domain.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PresenterTest {
    @Mock private ArticleService articleService;
    @Mock private UserService userService;
    @Mock private StatisticsService statisticsService;
    private Presenter presenter;

    @BeforeEach
    void setUp() {
        presenter = new Presenter(articleService, userService, statisticsService);
    }

    @Test
    void propagatesLoadFailureForFxTask() {
        IllegalStateException failure = new IllegalStateException("Database unavailable");
        when(articleService.getAll()).thenThrow(failure);

        assertSame(failure, assertThrows(IllegalStateException.class, presenter::onGetArticles));
    }

    @Test
    void propagatesSaveFailureForFxTask() {
        IllegalArgumentException failure = new IllegalArgumentException("Invalid user");
        User user = user();
        doThrow(failure).when(userService).add(user);

        assertSame(failure, assertThrows(IllegalArgumentException.class, () -> presenter.onAddUser(user)));
    }

    @Test
    void onAddArticleReturnsSuccess() {
        Article article = article();

        assertTrue(presenter.onAddArticle(article));
        verify(articleService).add(article);
    }

    @Test
    void onAddArticlePropagatesError() {
        Article article = article();
        IllegalArgumentException failure = new IllegalArgumentException("Author with ID 7 does not exist");
        doThrow(failure).when(articleService).add(article);

        assertSame(failure, assertThrows(IllegalArgumentException.class, () -> presenter.onAddArticle(article)));
    }

    @Test
    void onGetArticleByIdReturnsArticle() {
        Article article = article();
        when(articleService.getById(1)).thenReturn(article);

        assertSame(article, presenter.onGetArticleById(1));
    }

    @Test
    void onGetArticleByIdPropagatesError() {
        IllegalArgumentException failure = new IllegalArgumentException("No article found");
        when(articleService.getById(1)).thenThrow(failure);

        assertSame(failure, assertThrows(IllegalArgumentException.class, () -> presenter.onGetArticleById(1)));
    }

    @Test
    void onEditArticleDelegatesToService() {
        presenter.onEditArticle(1, "New title", "Updated content", Article.Status.MODERATING);

        verify(articleService).edit(1, "New title", "Updated content", Article.Status.MODERATING);
    }

    @Test
    void onEditArticleKeepsTitleAndContentWhenOnlyStatusChanges() {
        presenter.onEditArticle(1, "Title", "Long enough", Article.Status.MODERATING);

        verify(articleService).edit(1, "Title", "Long enough", Article.Status.MODERATING);
    }

    @Test
    void onEditArticlePropagatesError() {
        IllegalArgumentException failure = new IllegalArgumentException("Published article must have a publication date");
        doThrow(failure).when(articleService).edit(1, "Title", "Long enough", Article.Status.PUBLISHED);

        assertSame(failure, assertThrows(IllegalArgumentException.class,
                () -> presenter.onEditArticle(1, "Title", "Long enough", Article.Status.PUBLISHED)));
    }

    @Test
    void onDeleteArticleDelegatesToService() {
        presenter.onDeleteArticle(1);

        verify(articleService).delete(1);
    }

    @Test
    void onSortArticlesReturnsServiceResult() {
        List<Article> articles = List.of(article());
        when(articleService.sortArticles(ArticleQuery.SortColumn.TITLE, ArticleQuery.SortDir.ASC)).thenReturn(articles);

        assertSame(articles, presenter.onSortArticles(ArticleQuery.SortColumn.TITLE, ArticleQuery.SortDir.ASC));
    }

    @Test
    void onSearchArticleReturnsServiceResult() {
        List<Article> articles = List.of(article());
        when(articleService.search("java")).thenReturn(articles);

        assertSame(articles, presenter.onSearchArticle("java"));
    }

    @Test
    void onSearchArticlePropagatesError() {
        IllegalArgumentException failure = new IllegalArgumentException("Search query must contain a word");
        when(articleService.search("...")).thenThrow(failure);

        assertSame(failure, assertThrows(IllegalArgumentException.class, () -> presenter.onSearchArticle("...")));
    }

    @Test
    void onSortArticlesPropagatesError() {
        IllegalArgumentException failure = new IllegalArgumentException("Sort column is required");
        when(articleService.sortArticles(null, ArticleQuery.SortDir.ASC)).thenThrow(failure);

        assertSame(failure, assertThrows(IllegalArgumentException.class,
                () -> presenter.onSortArticles(null, ArticleQuery.SortDir.ASC)));
    }

    @Test
    void onFilterArticlesPropagatesError() {
        IllegalArgumentException failure = new IllegalArgumentException("Publication date must use YYYY-MM-DD format");
        when(articleService.filterArticles(ArticleQuery.FilterColumn.PUBLISHED_AT, "bad")).thenThrow(failure);

        assertSame(failure, assertThrows(IllegalArgumentException.class,
                () -> presenter.onFilterArticles(ArticleQuery.FilterColumn.PUBLISHED_AT, "bad")));
    }

    @Test
    void onFilterArticlesReturnsServiceResult() {
        List<Article> articles = List.of(article());
        when(articleService.filterArticles(ArticleQuery.FilterColumn.PUBLISHED_AT, "2026-09-27")).thenReturn(articles);

        assertSame(articles, presenter.onFilterArticles(ArticleQuery.FilterColumn.PUBLISHED_AT, "2026-09-27"));
    }

    @Test
    void onAddUserPropagatesError() {
        User user = user();
        IllegalArgumentException failure = new IllegalArgumentException("Email is invalid");
        doThrow(failure).when(userService).add(user);

        assertSame(failure, assertThrows(IllegalArgumentException.class, () -> presenter.onAddUser(user)));
    }

    @Test
    void onGetUserByIdReturnsUser() {
        User user = user();
        when(userService.getById(2)).thenReturn(user);

        assertSame(user, presenter.onGetUserById(2));
    }

    @Test
    void onEditUserDelegatesToService() {
        presenter.onEditUser(2, "bob", "bob@example.com", "hash", User.Role.EDITOR);

        verify(userService).edit(2, "bob", "bob@example.com", "hash", User.Role.EDITOR);
    }

    @Test
    void onDeleteUserDelegatesToService() {
        presenter.onDeleteUser(2);

        verify(userService).delete(2);
    }

    @Test
    void onExportArticlesPropagatesError() {
        IllegalStateException failure = new IllegalStateException("No articles to export");
        when(articleService.exportToExcel("articles.xlsx")).thenThrow(failure);

        assertSame(failure, assertThrows(IllegalStateException.class, () -> presenter.onExportArticles("articles.xlsx")));
    }

    @Test
    void onExportUsersPropagatesError() {
        IllegalStateException failure = new IllegalStateException("No users to export");
        when(userService.exportToExcel("users.xlsx")).thenThrow(failure);

        assertSame(failure, assertThrows(IllegalStateException.class, () -> presenter.onExportUsers("users.xlsx")));
    }

    private static Article article() {
        return new Article(1, 7, "Title", "Long enough", Article.Status.PENDING, null);
    }

    private static User user() {
        return new User(2, "alice", "alice@example.com", "hash", User.Role.AUTHOR);
    }
}

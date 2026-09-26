package presentation;

import domain.model.Article;
import domain.model.User;
import domain.service.ArticleService;
import domain.service.StatisticsService;
import domain.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PresenterTest {
    private final RecordingView view = new RecordingView();

    @Mock
    private ArticleService articleService;
    @Mock
    private UserService userService;
    @Mock
    private StatisticsService statisticsService;

    private Presenter presenter;

    @BeforeEach
    void setUp() {
        presenter = new Presenter(view, articleService, userService, statisticsService);
    }

    @Test
    void onAddArticleShowsSuccess() {
        Article article = article();

        assertTrue(presenter.onAddArticle(article));
        verify(articleService).add(article);
        assertEquals(List.of("Article added"), view.messages);
    }

    @Test
    void onAddArticleShowsErrorAndReturnsFalse() {
        Article article = article();
        doThrow(new IllegalArgumentException("Author with ID 7 does not exist"))
                .when(articleService).add(article);

        assertFalse(presenter.onAddArticle(article));
        assertEquals(List.of("Author with ID 7 does not exist"), view.errors);
    }

    @Test
    void onGetArticleByIdReturnsArticle() {
        Article article = article();
        when(articleService.getById(1)).thenReturn(article);

        assertSame(article, presenter.onGetArticleById(1));
    }

    @Test
    void onGetArticleByIdShowsErrorAndReturnsNull() {
        when(articleService.getById(1)).thenThrow(new IllegalArgumentException("No article found"));

        assertNull(presenter.onGetArticleById(1));
        assertEquals(List.of("No article found"), view.errors);
    }

    @Test
    void onDeleteArticleShowsSuccess() {
        presenter.onDeleteArticle(1);

        verify(articleService).delete(1);
        assertEquals(List.of("Article deleted"), view.messages);
    }

    @Test
    void onAddUserShowsErrorFromService() {
        User user = user();
        doThrow(new IllegalArgumentException("Email is invalid"))
                .when(userService).add(user);

        presenter.onAddUser(user);

        assertEquals(List.of("Email is invalid"), view.errors);
    }

    @Test
    void onGetUserByIdReturnsUser() {
        User user = user();
        when(userService.getById(2)).thenReturn(user);

        assertSame(user, presenter.onGetUserById(2));
    }

    @Test
    void onEditUserShowsSuccess() {
        presenter.onEditUser(2, "bob", "bob@example.com", "hash", User.Role.EDITOR);

        verify(userService).edit(2, "bob", "bob@example.com", "hash", User.Role.EDITOR);
        assertEquals(List.of("User edited"), view.messages);
    }

    @Test
    void onDeleteUserShowsSuccess() {
        presenter.onDeleteUser(2);

        verify(userService).delete(2);
        assertEquals(List.of("User deleted"), view.messages);
    }

    private static Article article() {
        return new Article(1, 7, "Title", "Long enough", Article.Status.PENDING, null);
    }

    private static User user() {
        return new User(2, "alice", "alice@example.com", "hash", User.Role.AUTHOR);
    }

    private static final class RecordingView implements View {
        private final List<String> messages = new ArrayList<>();
        private final List<String> errors = new ArrayList<>();

        @Override
        public void showStartOptions() {
        }

        @Override
        public void showArticle(Article article) {
        }

        @Override
        public void showArticles() {
        }

        @Override
        public void showUser(User user) {
        }

        @Override
        public void showUsers() {
        }

        @Override
        public void showMessage(String message) {
            messages.add(message);
        }

        @Override
        public void showError(String error) {
            errors.add(error);
        }

        @Override
        public String getUserInput(String prompt) {
            return "";
        }

        @Override
        public int getMenuChoice() {
            return 0;
        }

        @Override
        public void showStats() {
        }
    }
}

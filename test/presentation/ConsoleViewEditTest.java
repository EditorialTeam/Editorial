package presentation;

import domain.model.Article;
import domain.model.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import presentation.validation.InputValidationService;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ConsoleViewEditTest {
    private final InputStream originalIn = System.in;
    private final PrintStream originalOut = System.out;

    private Presenter presenter;

    @BeforeEach
    void silenceOutput() {
        System.setOut(new PrintStream(new ByteArrayOutputStream(), true, StandardCharsets.UTF_8));
    }

    @AfterEach
    void restoreStreams() {
        System.setIn(originalIn);
        System.setOut(originalOut);
    }

    @Test
    void editArticleKeepsEveryFieldWhenInputIsEmpty() {
        ConsoleView view = viewWithInput(articleEdit("", "", ""));
        when(presenter.onGetArticleById(1)).thenReturn(article());

        view.run();

        verify(presenter).onEditArticle(1, "Title", "Long enough", Article.Status.PENDING);
    }

    @Test
    void editArticleReplacesOnlyFieldsThatWereEntered() {
        ConsoleView view = viewWithInput(articleEdit("Fresh title", "", "2"));
        when(presenter.onGetArticleById(1)).thenReturn(article());

        view.run();

        verify(presenter).onEditArticle(1, "Fresh title", "Long enough", Article.Status.MODERATING);
    }

    @Test
    void editArticleUsesEnteredValuesWithoutLoadingCurrentArticle() {
        ConsoleView view = viewWithInput(articleEdit("Fresh title", "Updated content", "2"));

        view.run();

        verify(presenter).onEditArticle(1, "Fresh title", "Updated content", Article.Status.MODERATING);
        verify(presenter, never()).onGetArticleById(1);
    }

    @Test
    void editUserKeepsEveryFieldWhenInputIsEmpty() {
        ConsoleView view = viewWithInput(userEdit("", "", "", ""));
        when(presenter.onGetUserById(2)).thenReturn(user());

        view.run();

        verify(presenter).onEditUser(2, "alice", "alice@example.com", "hash", User.Role.AUTHOR);
    }

    @Test
    void editUserReplacesOnlyFieldsThatWereEntered() {
        ConsoleView view = viewWithInput(userEdit("", "bob@example.com", "", "2"));
        when(presenter.onGetUserById(2)).thenReturn(user());

        view.run();

        verify(presenter).onEditUser(2, "alice", "bob@example.com", "hash", User.Role.EDITOR);
    }

    @Test
    void editUserUsesEnteredValuesWithoutLoadingCurrentUser() {
        ConsoleView view = viewWithInput(userEdit("bob", "bob@example.com", "new-hash", "2"));

        view.run();

        verify(presenter).onEditUser(2, "bob", "bob@example.com", "new-hash", User.Role.EDITOR);
        verify(presenter, never()).onGetUserById(2);
    }

    private ConsoleView viewWithInput(String input) {
        System.setIn(new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)));
        presenter = mock(Presenter.class);
        ConsoleView view = new ConsoleView(mock(InputValidationService.class));
        view.setPresenter(presenter);
        return view;
    }

    private static String articleEdit(String title, String content, String statusChoice) {
        return script("1", "3", "1", title, content, statusChoice);
    }

    private static String userEdit(String username, String email, String passwordHash, String roleChoice) {
        return script("2", "2", "2", username, email, passwordHash, roleChoice);
    }

    private static String script(String... lines) {
        return String.join("\n", lines) + "\n0\n0\n";
    }

    private static Article article() {
        return new Article(1, 7, "Title", "Long enough", Article.Status.PENDING, null);
    }

    private static User user() {
        return new User(2, "alice", "alice@example.com", "hash", User.Role.AUTHOR);
    }
}

package presentation;

import domain.model.Article;
import domain.model.ArticleQuery;
import domain.model.EditorialStatistics;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import presentation.validation.InputValidationService;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ConsoleViewTest {
    private final InputStream originalIn = System.in;
    private final PrintStream originalOut = System.out;
    private final ByteArrayOutputStream output = new ByteArrayOutputStream();

    private Presenter presenter;
    private ConsoleView view;

    @BeforeEach
    void setUp() {
        System.setOut(new PrintStream(output, true, StandardCharsets.UTF_8));
        presenter = mock(Presenter.class);
        view = new ConsoleView(null);
        view.setPresenter(presenter);
    }

    @AfterEach
    void restoreSystemOut() {
        System.setIn(originalIn);
        System.setOut(originalOut);
    }

    @Test
    void showStatsPrintsUserAndArticleCountsGroupedByStatus() {
        when(presenter.onGetStatistics()).thenReturn(
                new EditorialStatistics(2, 4, 1, 1, 1)
        );

        view.showStats();

        String result = output.toString(StandardCharsets.UTF_8);
        assertAll(
                () -> assertTrue(result.contains("User count: 2")),
                () -> assertTrue(result.contains("Article count: 4")),
                () -> assertTrue(result.contains("Articles awaiting moderation: 1")),
                () -> assertTrue(result.contains("Articles published: 1")),
                () -> assertTrue(result.contains("Articles rejected: 1"))
        );
        verify(presenter).onGetStatistics();
    }

    @Test
    void sortArticlesReadsOptionsAndShowsSortedArticles() {
        String input = String.join(
                System.lineSeparator(),
                "1", // Open articles menu
                "7", // Sort articles
                "2", // Sort by title
                "1", // Ascending
                "0", // Back to main menu
                "0"  // Exit
        ) + System.lineSeparator();
        System.setIn(new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)));

        Article article = new Article(
                1,
                7,
                "Alphabetical title",
                "Long enough content",
                Article.Status.PUBLISHED,
                "2026-09-27"
        );
        when(presenter.onSortArticles(
                ArticleQuery.SortColumn.TITLE,
                ArticleQuery.SortDir.ASC
        )).thenReturn(List.of(article));

        ConsoleView sortingView = new ConsoleView(null);
        sortingView.setPresenter(presenter);
        sortingView.run();

        verify(presenter).onSortArticles(
                ArticleQuery.SortColumn.TITLE,
                ArticleQuery.SortDir.ASC
        );
        assertTrue(output.toString(StandardCharsets.UTF_8).contains("Title: Alphabetical title"));
    }

    @Test
    void filterArticlesReadsPublicationDateAndShowsMatches() {
        String input = String.join(
                System.lineSeparator(),
                "1",          // Open articles menu
                "6",          // Filter articles
                "1",          // Filter by publication date
                "2026-09-27", // Publication date
                "0",          // Back to main menu
                "0"           // Exit
        ) + System.lineSeparator();
        System.setIn(new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)));

        Article article = new Article(
                1,
                7,
                "Alphabetical title",
                "Long enough content",
                Article.Status.PUBLISHED,
                "2026-09-27"
        );
        when(presenter.onFilterArticles(
                ArticleQuery.FilterColumn.PUBLISHED_AT,
                "2026-09-27"
        )).thenReturn(List.of(article));

        InputValidationService inputValidationService = mock(InputValidationService.class);
        ConsoleView filteringView = new ConsoleView(inputValidationService);
        filteringView.setPresenter(presenter);
        filteringView.run();

        verify(inputValidationService).validatePublicationDate("2026-09-27");
        verify(presenter).onFilterArticles(
                ArticleQuery.FilterColumn.PUBLISHED_AT,
                "2026-09-27"
        );
        assertTrue(output.toString(StandardCharsets.UTF_8).contains("Title: Alphabetical title"));
    }
}

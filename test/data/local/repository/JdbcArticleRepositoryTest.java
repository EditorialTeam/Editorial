package data.local.repository;

import data.local.JdbcTestBase;
import domain.model.Article;
import domain.model.ArticleQuery;
import domain.model.User;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class JdbcArticleRepositoryTest extends JdbcTestBase {
    @Test
    void addArticleAssignsIdAndGetArticleByIdReadsFields() {
        User author = persistAuthor();
        Article article = new Article(
                0,
                author.getId(),
                "Draft title",
                "Draft content",
                Article.Status.PENDING,
                null
        );

        articleRepository.addArticle(article);

        assertTrue(article.getId() > 0);
        Article stored = articleRepository.getArticleById(article.getId());
        assertEquals(author.getId(), stored.getAuthorId());
        assertEquals("Draft title", stored.getTitle());
        assertEquals("Draft content", stored.getContent());
        assertEquals(Article.Status.PENDING, stored.getStatus());
    }

    @Test
    void addArticlePersistsPublicationDate() {
        User author = persistAuthor();
        Article article = new Article(
                0,
                author.getId(),
                "Ready title",
                "Ready content",
                Article.Status.PUBLISHED,
                "2026-09-26T12:00:00Z"
        );

        articleRepository.addArticle(article);

        Article stored = articleRepository.getArticleById(article.getId());
        assertEquals(Article.Status.PUBLISHED, stored.getStatus());
        assertTrue(stored.getPublishedAt().contains("2026-09-26"));
    }

    @Test
    void addArticleFailsWhenAuthorDoesNotExist() {
        Article article = new Article(0, 99, "Title", "Content", Article.Status.PENDING, null);

        assertThrows(IllegalStateException.class, () -> articleRepository.addArticle(article));
    }

    @Test
    void editArticleUpdatesTitleContentAndStatus() {
        User author = persistAuthor();
        Article article = new Article(0, author.getId(), "Old title", "Old content", Article.Status.PENDING, null);
        articleRepository.addArticle(article);

        article.setTitle("New title");
        article.setContent("New content");
        article.setStatus(Article.Status.MODERATING);
        articleRepository.editArticle(article);

        Article stored = articleRepository.getArticleById(article.getId());
        assertEquals("New title", stored.getTitle());
        assertEquals("New content", stored.getContent());
        assertEquals(Article.Status.MODERATING, stored.getStatus());
    }

    @Test
    void editArticlePersistsStatusChangeOnly() {
        User author = persistAuthor();
        Article article = new Article(0, author.getId(), "Draft title", "Draft content", Article.Status.PENDING, null);
        articleRepository.addArticle(article);

        article.setStatus(Article.Status.REJECTED);
        articleRepository.editArticle(article);

        Article stored = articleRepository.getArticleById(article.getId());
        assertEquals(Article.Status.REJECTED, stored.getStatus());
        assertEquals("Draft title", stored.getTitle());
        assertEquals("Draft content", stored.getContent());
    }

    @Test
    void editArticleThrowsWhenArticleDoesNotExist() {
        Article missing = new Article(99, 1, "Title", "Content", Article.Status.PENDING, null);

        assertThrows(IllegalArgumentException.class, () -> articleRepository.editArticle(missing));
    }

    @Test
    void deleteArticleRemovesRow() {
        User author = persistAuthor();
        Article article = new Article(0, author.getId(), "Title", "Content", Article.Status.PENDING, null);
        articleRepository.addArticle(article);

        articleRepository.deleteArticle(article.getId());

        assertThrows(IllegalArgumentException.class, () -> articleRepository.getArticleById(article.getId()));
    }

    @Test
    void deleteArticleThrowsWhenArticleDoesNotExist() {
        assertThrows(IllegalArgumentException.class, () -> articleRepository.deleteArticle(42));
    }

    @Test
    void getArticleByIdThrowsWhenArticleDoesNotExist() {
        assertThrows(IllegalArgumentException.class, () -> articleRepository.getArticleById(7));
    }

    @Test
    void sortArticlesReturnsFullyMappedArticlesInRequestedOrder() {
        User author = persistAuthor();
        articleRepository.addArticle(new Article(
                0,
                author.getId(),
                "Zebra title",
                "Zebra content",
                Article.Status.PENDING,
                null
        ));
        articleRepository.addArticle(new Article(
                0,
                author.getId(),
                "Alpha title",
                "Alpha content",
                Article.Status.PUBLISHED,
                "2026-09-27T12:00:00Z"
        ));

        var articles = articleRepository.sortArticles(
                ArticleQuery.SortColumn.TITLE,
                ArticleQuery.SortDir.ASC
        );

        assertEquals(2, articles.size());
        assertEquals("Alpha title", articles.get(0).getTitle());
        assertEquals(Article.Status.PUBLISHED, articles.get(0).getStatus());
        assertEquals("Zebra title", articles.get(1).getTitle());
        assertEquals(Article.Status.PENDING, articles.get(1).getStatus());
    }

    @Test
    void filterArticlesSupportsPublicationDatesAndExactStatuses() {
        User author = persistAuthor();
        articleRepository.addArticle(new Article(
                0,
                author.getId(),
                "Learning PostgreSQL",
                "Database content",
                Article.Status.PUBLISHED,
                "2026-09-27T12:00:00Z"
        ));
        articleRepository.addArticle(new Article(
                0,
                author.getId(),
                "Java basics",
                "Java content",
                Article.Status.PENDING,
                null
        ));

        var dateMatches = articleRepository.filterArticles(
                ArticleQuery.FilterColumn.PUBLISHED_AT,
                "2026-09-27"
        );
        var statusMatches = articleRepository.filterArticles(
                ArticleQuery.FilterColumn.STATUS,
                "pending"
        );

        assertEquals(1, dateMatches.size());
        assertEquals("Learning PostgreSQL", dateMatches.get(0).getTitle());
        assertEquals(Article.Status.PUBLISHED, dateMatches.get(0).getStatus());
        assertEquals(1, statusMatches.size());
        assertEquals("Java basics", statusMatches.get(0).getTitle());
        assertEquals(Article.Status.PENDING, statusMatches.get(0).getStatus());
    }

    @Test
    void searchArticleRanksByKeywordMatchesAndIgnoresPunctuation() {
        User javaAuthor = new User(0, "Java Expert", "java@example.com", "hash", User.Role.AUTHOR);
        User other = new User(0, "editor", "editor@example.com", "hash", User.Role.EDITOR);
        userRepository.addUser(javaAuthor);
        userRepository.addUser(other);

        articleRepository.addArticle(new Article(
                0,
                other.getId(),
                "Notes",
                "Nothing relevant",
                Article.Status.PENDING,
                null
        ));
        articleRepository.addArticle(new Article(
                0,
                other.getId(),
                "Java!",
                "A short note.",
                Article.Status.PENDING,
                null
        ));
        articleRepository.addArticle(new Article(
                0,
                javaAuthor.getId(),
                "Guide",
                "Java, java and more java.",
                Article.Status.PENDING,
                null
        ));

        var found = articleRepository.searchArticle("java,");

        assertEquals(2, found.size());
        assertEquals("Guide", found.get(0).getTitle());
        assertEquals("Java!", found.get(1).getTitle());
        assertThrows(IllegalArgumentException.class, () -> articleRepository.searchArticle("..."));
    }

    @Test
    void searchArticleRanksPartialWordMatchesBelowExactMatches() {
        User author = persistAuthor();
        articleRepository.addArticle(new Article(
                0,
                author.getId(),
                "Javascript notes",
                "A language overview",
                Article.Status.PENDING,
                null
        ));
        articleRepository.addArticle(new Article(
                0,
                author.getId(),
                "Java",
                "A short note",
                Article.Status.PENDING,
                null
        ));
        articleRepository.addArticle(new Article(
                0,
                author.getId(),
                "Unrelated",
                "Nothing here",
                Article.Status.PENDING,
                null
        ));

        var found = articleRepository.searchArticle("java");

        assertEquals(2, found.size());
        assertEquals("Java", found.get(0).getTitle());
        assertEquals("Javascript notes", found.get(1).getTitle());
    }

    @Test
    void searchArticleMatchesAuthorTitleAndContentAndRanksMoreKeywordsHigher() {
        User springAuthor = new User(0, "Spring Fan", "spring@example.com", "hash", User.Role.AUTHOR);
        User other = new User(0, "editor", "editor@example.com", "hash", User.Role.EDITOR);
        userRepository.addUser(springAuthor);
        userRepository.addUser(other);

        addArticle(other, "Bootcamp", "A partial mention only", Article.Status.PENDING, null);
        addArticle(other, "Spring", "Notes without the second word", Article.Status.PENDING, null);
        addArticle(springAuthor, "Guide", "Boot setup for beginners", Article.Status.PENDING, null);

        var found = articleRepository.searchArticle("spring, boot");

        assertEquals(List.of("Guide", "Spring", "Bootcamp"), titles(found));
    }

    @Test
    void searchArticleRejectsBlankQuery() {
        assertThrows(IllegalArgumentException.class, () -> articleRepository.searchArticle("   "));
        assertThrows(IllegalArgumentException.class, () -> articleRepository.searchArticle(null));
    }

    @Test
    void sortArticlesOrdersByIdTitleAndPublicationDate() {
        User author = persistAuthor();
        Article first = addArticle(author, "Zebra", "Content", Article.Status.PENDING, null);
        Article second = addArticle(author, "Alpha", "Content", Article.Status.PUBLISHED, "2026-09-26T12:00:00Z");
        Article third = addArticle(author, "Middle", "Content", Article.Status.PUBLISHED, "2026-09-28T12:00:00Z");

        assertEquals(
                List.of(first.getId(), second.getId(), third.getId()),
                ids(articleRepository.sortArticles(ArticleQuery.SortColumn.ID, ArticleQuery.SortDir.ASC))
        );
        assertEquals(
                List.of("Zebra", "Middle", "Alpha"),
                titles(articleRepository.sortArticles(ArticleQuery.SortColumn.TITLE, ArticleQuery.SortDir.DESC))
        );
        assertEquals(
                List.of("Middle", "Alpha", "Zebra"),
                titles(articleRepository.sortArticles(ArticleQuery.SortColumn.PUBLISHED_AT, ArticleQuery.SortDir.DESC))
        );
    }

    @Test
    void sortArticlesRejectsMissingColumnOrDirection() {
        assertThrows(
                IllegalArgumentException.class,
                () -> articleRepository.sortArticles(null, ArticleQuery.SortDir.ASC)
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> articleRepository.sortArticles(ArticleQuery.SortColumn.ID, null)
        );
    }

    @Test
    void filterArticlesReturnsMatchesInIdOrderAndRejectsInvalidInput() {
        User author = persistAuthor();
        addArticle(author, "Later", "Content", Article.Status.PUBLISHED, "2026-09-28T08:00:00Z");
        addArticle(author, "Earlier", "Content", Article.Status.PUBLISHED, "2026-09-27T08:00:00Z");
        addArticle(author, "Same day", "Content", Article.Status.REJECTED, "2026-09-27T20:00:00Z");

        assertEquals(
                List.of("Earlier", "Same day"),
                titles(articleRepository.filterArticles(ArticleQuery.FilterColumn.PUBLISHED_AT, " 2026-09-27 "))
        );
        assertEquals(
                List.of("Later", "Earlier"),
                titles(articleRepository.filterArticles(ArticleQuery.FilterColumn.STATUS, " published "))
        );
        assertEquals(
                List.of(),
                articleRepository.filterArticles(ArticleQuery.FilterColumn.PUBLISHED_AT, "2026-01-01")
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> articleRepository.filterArticles(ArticleQuery.FilterColumn.PUBLISHED_AT, "27.09.2026")
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> articleRepository.filterArticles(ArticleQuery.FilterColumn.STATUS, " ")
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> articleRepository.filterArticles(null, "PENDING")
        );
    }

    private static Article addArticle(
            User author,
            String title,
            String content,
            Article.Status status,
            String publishedAt
    ) {
        Article article = new Article(0, author.getId(), title, content, status, publishedAt);
        articleRepository.addArticle(article);
        return article;
    }

    private static List<String> titles(List<Article> articles) {
        return articles.stream().map(Article::getTitle).toList();
    }

    private static List<Integer> ids(List<Article> articles) {
        return articles.stream().map(Article::getId).toList();
    }

    private static User persistAuthor() {
        User author = new User(0, "author", "author@example.com", "hash", User.Role.AUTHOR);
        userRepository.addUser(author);
        return author;
    }
}

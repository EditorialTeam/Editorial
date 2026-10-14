package domain.service;

import domain.model.Article;
import domain.model.EditorialStatistics;
import domain.model.User;
import domain.repository.ArticleRepository;
import domain.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StatisticsServiceTest {
    @Mock
    private ArticleRepository articleRepository;
    @Mock
    private UserRepository userRepository;

    @Test
    void calculatesStatisticsFromRepositories() {
        when(userRepository.getUsers()).thenReturn(List.of(user(1), user(2)));
        when(articleRepository.getArticles()).thenReturn(List.of(
                article(1, Article.Status.PENDING),
                article(2, Article.Status.MODERATING),
                article(3, Article.Status.PUBLISHED),
                article(4, Article.Status.REJECTED)
        ));

        EditorialStatistics statistics =
                new StatisticsService(articleRepository, userRepository).getStatistics();

        assertEquals(2, statistics.getUserCount());
        assertEquals(4, statistics.getArticleCount());
        assertEquals(1, statistics.getPendingArticleCount());
        assertEquals(1, statistics.getPublishedArticleCount());
        assertEquals(1, statistics.getRejectedArticleCount());
    }

    private static User user(int id) {
        return new User(id, "user" + id, "user" + id + "@example.com", "hash", User.Role.AUTHOR);
    }

    private static Article article(int id, Article.Status status) {
        return new Article(id, 1, "Title", "Content", status, null);
    }
}

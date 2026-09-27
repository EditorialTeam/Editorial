package domain.service;

import domain.model.Article;
import domain.model.EditorialStatistics;
import domain.repository.ArticleRepository;
import domain.repository.UserRepository;

import java.util.List;

public class StatisticsService {
    private final ArticleRepository articleRepository;
    private final UserRepository userRepository;

    public StatisticsService(
            ArticleRepository articleRepository,
            UserRepository userRepository
    ) {
        this.articleRepository = articleRepository;
        this.userRepository = userRepository;
    }

    public EditorialStatistics getStatistics() {
        List<Article> articles = articleRepository.getArticles();

        int pendingCount = 0;
        int moderatingCount = 0;
        int publishedCount = 0;
        int rejectedCount = 0;

        for (Article article : articles) {
            if (article.getStatus() == Article.Status.PENDING) {
                pendingCount++;
            } else if (article.getStatus() == Article.Status.MODERATING) {
                moderatingCount++;
            } else if (article.getStatus() == Article.Status.PUBLISHED) {
                publishedCount++;
            } else if (article.getStatus() == Article.Status.REJECTED) {
                rejectedCount++;
            }
        }

        return new EditorialStatistics(
                userRepository.getUsers().size(),
                articles.size(),
                pendingCount,
                moderatingCount,
                publishedCount,
                rejectedCount
        );
    }
}

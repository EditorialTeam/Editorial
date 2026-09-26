package domain.model;

public record EditorialStatistics(
        int userCount,
        int articleCount,
        int pendingArticleCount,
        int publishedArticleCount,
        int rejectedArticleCount
) {
    public static EditorialStatistics empty() {
        return new EditorialStatistics(0, 0, 0, 0, 0);
    }
}

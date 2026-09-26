package domain.model;

public final class EditorialStatistics {
    private final int userCount;
    private final int articleCount;
    private final int pendingArticleCount;
    private final int publishedArticleCount;
    private final int rejectedArticleCount;

    public EditorialStatistics(
            int userCount,
            int articleCount,
            int pendingArticleCount,
            int publishedArticleCount,
            int rejectedArticleCount
    ) {
        this.userCount = userCount;
        this.articleCount = articleCount;
        this.pendingArticleCount = pendingArticleCount;
        this.publishedArticleCount = publishedArticleCount;
        this.rejectedArticleCount = rejectedArticleCount;
    }

    public static EditorialStatistics empty() {
        return new EditorialStatistics(0, 0, 0, 0, 0);
    }

    public int getUserCount() {
        return userCount;
    }

    public int getArticleCount() {
        return articleCount;
    }

    public int getPendingArticleCount() {
        return pendingArticleCount;
    }

    public int getPublishedArticleCount() {
        return publishedArticleCount;
    }

    public int getRejectedArticleCount() {
        return rejectedArticleCount;
    }

}

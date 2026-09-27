package domain.model;

public class ArticleQuery {
    public enum SortDir {
        ASC,
        DESC
    }

    public enum SortColumn {
        ID,
        TITLE,
        PUBLISHED_AT
    }
}

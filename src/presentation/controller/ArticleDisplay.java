package presentation.controller;

import domain.model.Article;
import domain.model.User;
import javafx.scene.control.ListCell;
import javafx.util.StringConverter;

import java.util.List;
import java.util.Locale;

public final class ArticleDisplay {
    private ArticleDisplay() {}

    public static String status(Article.Status status) {
        if (status == null) return "—";
        return switch (status) {
            case PENDING -> "Ожидает рассмотрения";
            case MODERATING -> "На редактуре";
            case REJECTED -> "Отклонено";
            case PUBLISHED -> "Опубликовано";
        };
    }

    public static String date(Article article) {
        String date = article.getPublishedAt();
        return date == null || date.isBlank() ? "Не опубликовано" : date.substring(0, Math.min(10, date.length()));
    }

    public static String author(Article article, List<User> users) {
        return users.stream().filter(user -> user.getId() == article.getAuthorId())
                .map(User::getUsername).findFirst().orElse("Автор #" + article.getAuthorId());
    }

    public static String excerpt(String content) {
        String text = content == null ? "" : content.replaceAll("\\s+", " ").strip();
        return text.length() > 180 ? text.substring(0, 180) + "…" : text;
    }

    public static boolean matches(Article article, String query, Article.Status status, User author) {
        String text = (article.getId() + " " + article.getTitle() + " " + article.getContent())
                .toLowerCase(Locale.ROOT);
        return text.contains(query.strip().toLowerCase(Locale.ROOT))
                && (status == null || article.getStatus() == status)
                && (author == null || article.getAuthorId() == author.getId());
    }

    public static final StringConverter<Article.Status> STATUS_CONVERTER = new StringConverter<>() {
        public String toString(Article.Status value) { return value == null ? "Все статусы" : status(value); }
        public Article.Status fromString(String value) { throw new UnsupportedOperationException(); }
    };

    public static final StringConverter<User> USER_CONVERTER = new StringConverter<>() {
        public String toString(User value) { return value == null ? "Все авторы" : value.getUsername() + " (#" + value.getId() + ")"; }
        public User fromString(String value) { throw new UnsupportedOperationException(); }
    };

    public static <T> ListCell<T> cell(StringConverter<T> converter) {
        return new ListCell<>() {
            @Override
            protected void updateItem(T value, boolean empty) {
                super.updateItem(value, empty);
                setText(empty ? null : converter.toString(value));
            }
        };
    }
}

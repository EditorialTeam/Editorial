package domain.model;

// Класс статьи
public class Article {
    // Свойства
    private int id;
    private int authorId;
    private String title;
    private String content;
    private Status status;
    private String publishedAt;

    // Конструктор класса
    public Article(int id, int authorId, String title, String content, Status status, String publishedAt) {
        this.id = id;
        this.authorId = authorId;
        this.title = title;
        this.content = content;
        this.status = status;
        this.publishedAt = publishedAt;
    }

    // Геттеры
    public int getId() {
        return id;
    }

    public int getAuthorId() {
        return authorId;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public Status getStatus() {
        return status;
    }

    public String getPublishedAt() {
        return publishedAt;
    }

    // Сеттеры
    public void setId(int id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public void setPublishedAt(String publishedAt) {
        this.publishedAt = publishedAt;
    }

    // enum Status
    public enum Status {
        PENDING, MODERATING, REJECTED, PUBLISHED
    }


}


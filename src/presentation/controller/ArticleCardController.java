package presentation.controller;

import domain.model.Article;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import presentation.util.NavigationManager;

public class ArticleCardController {
    @FXML private Label statusBadge, articleIdLabel, titleLabel, excerptLabel;
    @FXML private Label authorNameLabel, dateLabel;
    @FXML private Button readBtn, editBtn, deleteBtn;

    public void init(NavigationManager navigation, Article article, String author, Runnable refresh) {
        statusBadge.setText(ArticleDisplay.status(article.getStatus()));
        statusBadge.getStyleClass().removeIf(style -> style.startsWith("badge-"));
        if (article.getStatus() != null) {
            statusBadge.getStyleClass().add("badge-" + article.getStatus().name().toLowerCase(java.util.Locale.ROOT));
        }
        articleIdLabel.setText("№ " + article.getId());
        titleLabel.setText(article.getTitle());
        excerptLabel.setText(ArticleDisplay.excerpt(article.getContent()));
        authorNameLabel.setText(author);
        dateLabel.setText(ArticleDisplay.date(article));
        readBtn.setOnAction(event -> navigation.showReader(article));
        editBtn.setOnAction(event -> navigation.openArticleDialog(article, refresh));
        deleteBtn.setOnAction(event -> navigation.deleteArticle(article, refresh));
    }
}

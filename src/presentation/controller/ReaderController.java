package presentation.controller;

import domain.model.Article;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import presentation.util.NavigationManager;

public class ReaderController extends FxController {
    @FXML private Button backBtn, fontDecBtn, fontIncBtn, editBtn;
    @FXML private Label fontIndicator, statusPill, articleIdLabel, titleLabel, authorName;
    @FXML private Label dateLabel, bodyText;
    private int fontSize = 18;

    public void init(NavigationManager navigation, Article article) {
        connect(navigation);
        backBtn.setOnAction(event -> navigation.showCatalog());
        titleLabel.setText(article.getTitle());
        statusPill.setText(ArticleDisplay.status(article.getStatus()));
        statusPill.getStyleClass().removeIf(style -> style.startsWith("badge-"));
        articleIdLabel.setText("Статья № " + article.getId());
        authorName.setText("Автор #" + article.getAuthorId());
        editBtn.setOnAction(event -> navigation.openArticleDialog(article, () ->
                background(() -> presenter.onGetArticleById(article.getId()), updated -> {
                    if (updated != null) navigation.showReader(updated);
                })));
        dateLabel.setText(ArticleDisplay.date(article));
        bodyText.setText(article.getContent() == null ? "" : article.getContent());
        fontDecBtn.setOnAction(event -> resize(-2));
        fontIncBtn.setOnAction(event -> resize(2));
        resize(0);
        background(() -> presenter.onGetUserById(article.getAuthorId()), user -> {
            if (user != null) {
                authorName.setText(user.getUsername());
            }
        });
    }

    private void resize(int delta) {
        fontSize = Math.max(12, Math.min(30, fontSize + delta));
        bodyText.setStyle("-fx-font-size: " + fontSize + "px;");
        fontIndicator.setText(Math.round(fontSize * 100.0 / 18) + "%");
        fontDecBtn.setDisable(fontSize == 12);
        fontIncBtn.setDisable(fontSize == 30);
    }
}

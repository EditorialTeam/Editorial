package presentation.controller.dialog;

import domain.model.Article;
import domain.model.User;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import presentation.controller.ArticleDisplay;
import presentation.controller.FxController;
import presentation.util.NavigationManager;

import java.time.Instant;

public class ArticleDialogController extends FxController {
    @FXML private Label dialogTitleLabel, errorLabel;
    @FXML private TextField titleField;
    @FXML private TextArea contentArea;
    @FXML private ComboBox<User> authorComboBox;
    @FXML private ComboBox<Article.Status> statusComboBox;
    @FXML private Button cancelButton, saveButton;
    private Stage stage;
    private Article original;
    private Runnable onSaveSuccess;
    private boolean saving;

    public void init(NavigationManager navigation, Stage stage, Article article, Runnable onSaveSuccess) {
        connect(navigation);
        this.stage = stage;
        this.original = article;
        this.onSaveSuccess = onSaveSuccess;
        dialogTitleLabel.setText(article == null ? "Новая статья" : "Редактирование статьи № " + article.getId());
        statusComboBox.getItems().setAll(Article.Status.values());
        statusComboBox.setConverter(ArticleDisplay.STATUS_CONVERTER);
        statusComboBox.setCellFactory(list -> ArticleDisplay.cell(ArticleDisplay.STATUS_CONVERTER));
        statusComboBox.setValue(article == null ? Article.Status.PENDING : article.getStatus());
        authorComboBox.setConverter(ArticleDisplay.USER_CONVERTER);
        authorComboBox.setCellFactory(list -> ArticleDisplay.cell(ArticleDisplay.USER_CONVERTER));
        if (article != null) {
            titleField.setText(article.getTitle());
            contentArea.setText(article.getContent());
            authorComboBox.setDisable(true);
        }
        stage.setOnCloseRequest(event -> { if (saving) event.consume(); });
        saveButton.setDisable(true);
        background(presenter::onGetUsers, users -> {
            authorComboBox.getItems().setAll(users);
            if (article != null) {
                authorComboBox.setValue(users.stream().filter(user -> user.getId() == article.getAuthorId()).findFirst().orElse(null));
            }
            saveButton.setDisable(users.isEmpty());
            if (users.isEmpty()) displayError("Сначала добавьте автора на экране «Пользователи»");
        }, error -> displayError(error.getMessage()));
    }

    @FXML
    private void onSave() {
        if (saving) return;
        User author = authorComboBox.getValue();
        Article.Status status = statusComboBox.getValue();
        if (author == null || status == null) {
            displayError("Выберите автора и статус");
            return;
        }
        String title = titleField.getText().strip();
        String content = contentArea.getText();
        saving = true;
        saveButton.setDisable(true);
        cancelButton.setDisable(true);
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
        background(() -> {
            if (original == null) {
                Article article = new Article(0, author.getId(), title, content, status,
                        status == Article.Status.PUBLISHED ? Instant.now().toString() : null);
                presenter.onAddArticle(article);
            } else {
                presenter.onEditArticle(original.getId(), title, content, status);
            }
            return true;
        }, result -> {
            saving = false;
            stage.close();
            if (onSaveSuccess != null) onSaveSuccess.run();
        }, error -> {
            saving = false;
            saveButton.setDisable(false);
            cancelButton.setDisable(false);
            displayError(error.getMessage());
        });
    }

    private void displayError(String message) {
        errorLabel.setText(message == null ? "Не удалось выполнить операцию" : message);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }

    @FXML
    private void onCancel() {
        if (!saving) stage.close();
    }
}

package presentation.controller;

import domain.model.Article;
import domain.model.User;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import presentation.util.NavigationManager;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;

public class CatalogController extends FxController {
    @FXML private TextField searchInput;
    @FXML private ComboBox<User> authorFilterCombo;
    @FXML private ComboBox<Article.Status> statusFilterCombo;
    @FXML private ComboBox<String> sortCombo;
    @FXML private Button gridViewBtn, tableViewBtn, resetFiltersBtn;
    @FXML private Label archiveStatusLabel, countLabel;
    @FXML private FlowPane articlesGrid;
    private List<Article> articles = List.of();
    private List<User> users = List.of();

    public void init(NavigationManager navigation) {
        connect(navigation);
        searchInput.setPromptText("Поиск по названию, тексту или ID…");
        statusFilterCombo.setConverter(ArticleDisplay.STATUS_CONVERTER);
        statusFilterCombo.setCellFactory(list -> ArticleDisplay.cell(ArticleDisplay.STATUS_CONVERTER));
        statusFilterCombo.getItems().add(null);
        statusFilterCombo.getItems().addAll(Article.Status.values());
        authorFilterCombo.setConverter(ArticleDisplay.USER_CONVERTER);
        authorFilterCombo.setCellFactory(list -> ArticleDisplay.cell(ArticleDisplay.USER_CONVERTER));
        sortCombo.getItems().setAll("Сначала новые", "Сначала старые", "По заголовку");
        sortCombo.getSelectionModel().selectFirst();
        searchInput.textProperty().addListener((obs, old, value) -> render());
        statusFilterCombo.valueProperty().addListener((obs, old, value) -> render());
        authorFilterCombo.valueProperty().addListener((obs, old, value) -> render());
        sortCombo.valueProperty().addListener((obs, old, value) -> render());
        tableViewBtn.setOnAction(event -> navigation.showTableView());
        gridViewBtn.setOnAction(event -> reload());
        resetFiltersBtn.setOnAction(event -> {
            searchInput.clear();
            statusFilterCombo.setValue(null);
            authorFilterCombo.setValue(null);
        });
        reload();
    }

    private record Data(List<Article> articles, List<User> users) {}

    private void reload() {
        archiveStatusLabel.setText("Загрузка…");
        gridViewBtn.setDisable(true);
        background(() -> new Data(presenter.onGetArticles(), presenter.onGetUsers()), data -> {
            articles = data.articles();
            users = data.users();
            authorFilterCombo.getItems().setAll(users);
            authorFilterCombo.getItems().add(0, null);
            gridViewBtn.setDisable(false);
            archiveStatusLabel.setText("Архив обновлён");
            navigation.setArticleCount(articles.size());
            render();
        }, error -> {
            gridViewBtn.setDisable(false);
            archiveStatusLabel.setText("Ошибка загрузки");
            navigation.setDatabaseError();
            showError(error);
        });
    }

    private void render() {
        Comparator<Article> order = switch (sortCombo.getSelectionModel().getSelectedIndex()) {
            case 1 -> Comparator.comparingInt(Article::getId);
            case 2 -> Comparator.comparing(Article::getTitle, String.CASE_INSENSITIVE_ORDER);
            default -> Comparator.comparingInt(Article::getId).reversed();
        };
        List<Article> visible = articles.stream().filter(article -> ArticleDisplay.matches(article,
                searchInput.getText(), statusFilterCombo.getValue(), authorFilterCombo.getValue()))
                .sorted(order).toList();
        articlesGrid.getChildren().clear();
        countLabel.setText("Статей: " + visible.size());
        if (visible.isEmpty()) {
            articlesGrid.getChildren().add(new Label("Публикации не найдены"));
            return;
        }
        for (Article article : visible) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/article-card.fxml"));
                javafx.scene.Node card = loader.load();
                ArticleCardController controller = loader.getController();
                controller.init(navigation, article, ArticleDisplay.author(article, users), this::reload);
                articlesGrid.getChildren().add(card);
            } catch (IOException error) {
                showError(error);
                break;
            }
        }
    }
}

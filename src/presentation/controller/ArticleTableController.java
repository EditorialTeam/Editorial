package presentation.controller;

import domain.model.Article;
import domain.model.User;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import presentation.util.NavigationManager;

import java.util.List;

public class ArticleTableController extends FxController {
    @FXML private TextField searchField;
    @FXML private ComboBox<Article.Status> statusFilterCombo;
    @FXML private ComboBox<User> authorFilterCombo;
    @FXML private Button gridViewBtn, addBtn, editBtn, deleteBtn, exportBtn;
    @FXML private Label countLabel;
    @FXML private TableView<Article> articleTable;
    @FXML private TableColumn<Article, Integer> idColumn;
    @FXML private TableColumn<Article, String> titleColumn, authorColumn, statusColumn, dateColumn;
    private final ObservableList<Article> articles = FXCollections.observableArrayList();
    private final FilteredList<Article> filtered = new FilteredList<>(articles);
    private List<User> users = List.of();

    public void init(NavigationManager navigation) {
        connect(navigation);
        idColumn.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(cell.getValue().getId()));
        titleColumn.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().getTitle()));
        authorColumn.setCellValueFactory(cell -> new ReadOnlyStringWrapper(ArticleDisplay.author(cell.getValue(), users)));
        statusColumn.setCellValueFactory(cell -> new ReadOnlyStringWrapper(ArticleDisplay.status(cell.getValue().getStatus())));
        dateColumn.setCellValueFactory(cell -> new ReadOnlyStringWrapper(ArticleDisplay.date(cell.getValue())));
        SortedList<Article> sorted = new SortedList<>(filtered);
        sorted.comparatorProperty().bind(articleTable.comparatorProperty());
        articleTable.setItems(sorted);
        statusFilterCombo.setConverter(ArticleDisplay.STATUS_CONVERTER);
        statusFilterCombo.setCellFactory(list -> ArticleDisplay.cell(ArticleDisplay.STATUS_CONVERTER));
        statusFilterCombo.getItems().add(null);
        statusFilterCombo.getItems().addAll(Article.Status.values());
        authorFilterCombo.setConverter(ArticleDisplay.USER_CONVERTER);
        authorFilterCombo.setCellFactory(list -> ArticleDisplay.cell(ArticleDisplay.USER_CONVERTER));
        searchField.textProperty().addListener((obs, old, value) -> filter());
        statusFilterCombo.valueProperty().addListener((obs, old, value) -> filter());
        authorFilterCombo.valueProperty().addListener((obs, old, value) -> filter());
        gridViewBtn.setOnAction(event -> navigation.showCatalog());
        addBtn.setOnAction(event -> navigation.openArticleDialog(null, this::reload));
        editBtn.disableProperty().bind(articleTable.getSelectionModel().selectedItemProperty().isNull());
        deleteBtn.disableProperty().bind(articleTable.getSelectionModel().selectedItemProperty().isNull());
        editBtn.setOnAction(event -> navigation.openArticleDialog(articleTable.getSelectionModel().getSelectedItem(), this::reload));
        deleteBtn.setOnAction(event -> {
            Article article = articleTable.getSelectionModel().getSelectedItem();
            if (article != null) navigation.deleteArticle(article, this::reload);
        });
        exportBtn.setOnAction(event -> navigation.exportArticles());
        articleTable.setRowFactory(table -> {
            TableRow<Article> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) navigation.showReader(row.getItem());
            });
            return row;
        });
        reload();
    }

    private record Data(List<Article> articles, List<User> users) {}

    private void reload() {
        articleTable.setDisable(true);
        countLabel.setText("Загрузка…");
        background(() -> new Data(presenter.onGetArticles(), presenter.onGetUsers()), data -> {
            users = data.users();
            authorFilterCombo.getItems().setAll(users);
            authorFilterCombo.getItems().add(0, null);
            articles.setAll(data.articles());
            articleTable.setDisable(false);
            filter();
            navigation.setArticleCount(data.articles().size());
        }, error -> {
            articleTable.setDisable(false);
            countLabel.setText("Ошибка загрузки");
            navigation.setDatabaseError();
            showError(error);
        });
    }

    private void filter() {
        filtered.setPredicate(article -> ArticleDisplay.matches(article, searchField.getText(),
                statusFilterCombo.getValue(), authorFilterCombo.getValue()));
        countLabel.setText("Показано записей: " + filtered.size());
    }
}

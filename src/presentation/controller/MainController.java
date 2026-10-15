package presentation.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import presentation.util.NavigationManager;

public class MainController extends FxController {
    @FXML private Button tabCatalog, tabTable, tabStats, tabUsers;
    @FXML private Button createNewArticleBtn;
    @FXML private Label statusDbLabel, statusStatsLabel;

    public void init(NavigationManager navigation) {
        connect(navigation);
        tabCatalog.setOnAction(event -> navigation.showCatalog());
        tabTable.setOnAction(event -> navigation.showTableView());
        tabStats.setOnAction(event -> navigation.showStatistics());
        tabUsers.setOnAction(event -> navigation.showUsers());
        createNewArticleBtn.setOnAction(event -> createArticle());
        statusDbLabel.setText("Проверка подключения…");
    }

    public void installShortcuts() {
        var accelerators = navigation.getPrimaryStage().getScene().getAccelerators();
        accelerators.put(new KeyCodeCombination(KeyCode.N, KeyCombination.CONTROL_DOWN), this::createArticle);
        accelerators.put(new KeyCodeCombination(KeyCode.E, KeyCombination.CONTROL_DOWN), navigation::exportArticles);
        accelerators.put(new KeyCodeCombination(KeyCode.F5), navigation::refreshCurrentView);
    }

    private void createArticle() {
        navigation.openArticleDialog(null, navigation::refreshCurrentView);
    }

    public void setActiveTab(String active) {
        Button[] tabs = {tabCatalog, tabTable, tabStats, tabUsers};
        String[] names = {"catalog", "table", "stats", "users"};
        for (int i = 0; i < names.length; i++) {
            tabs[i].getStyleClass().remove("nav-button-active");
            if (names[i].equals(active)) {
                tabs[i].getStyleClass().add("nav-button-active");
            }
        }
    }

    public void setDataStatus(int articles) {
        statusDbLabel.setText("Данные загружены");
        statusStatsLabel.setText("Статей в системе: " + articles);
    }

    public void setDatabaseError() {
        statusDbLabel.setText("Не удалось загрузить данные");
    }
}

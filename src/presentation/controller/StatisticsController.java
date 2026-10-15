package presentation.controller;

import domain.model.EditorialStatistics;
import javafx.fxml.FXML;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import presentation.util.NavigationManager;

public class StatisticsController extends FxController {
    @FXML private Button refreshBtn, backToCatalogBtn;
    @FXML private Label totalArticlesLabel, totalUsersLabel, publishedCountLabel, publishedPercentLabel;
    @FXML private Label moderatingCountLabel, pendingCountLabel, rejectedCountLabel;
    @FXML private PieChart statusPieChart;

    public void init(NavigationManager navigation) {
        connect(navigation);
        backToCatalogBtn.setOnAction(event -> navigation.showCatalog());
        refreshBtn.setOnAction(event -> reload());
        statusPieChart.setAnimated(false);
        reload();
    }

    private void reload() {
        refreshBtn.setDisable(true);
        background(presenter::onGetStatistics, stats -> {
            refreshBtn.setDisable(false);
            display(stats);
            navigation.setArticleCount(stats.getArticleCount());
        }, error -> {
            refreshBtn.setDisable(false);
            navigation.setDatabaseError();
            showError(error);
        });
    }

    private void display(EditorialStatistics stats) {
        totalArticlesLabel.setText(Integer.toString(stats.getArticleCount()));
        totalUsersLabel.setText(Integer.toString(stats.getUserCount()));
        publishedCountLabel.setText(Integer.toString(stats.getPublishedArticleCount()));
        moderatingCountLabel.setText(Integer.toString(stats.getModeratingArticleCount()));
        pendingCountLabel.setText(Integer.toString(stats.getPendingArticleCount()));
        rejectedCountLabel.setText(Integer.toString(stats.getRejectedArticleCount()));
        double percent = stats.getArticleCount() == 0 ? 0 : 100.0 * stats.getPublishedArticleCount() / stats.getArticleCount();
        publishedPercentLabel.setText(String.format(java.util.Locale.forLanguageTag("ru"), "%.1f%% от общего числа", percent));
        statusPieChart.getData().setAll(
                new PieChart.Data("Опубликовано", stats.getPublishedArticleCount()),
                new PieChart.Data("На редактуре", stats.getModeratingArticleCount()),
                new PieChart.Data("Ожидает", stats.getPendingArticleCount()),
                new PieChart.Data("Отклонено", stats.getRejectedArticleCount()));
        statusPieChart.getData().removeIf(data -> data.getPieValue() == 0);
    }
}

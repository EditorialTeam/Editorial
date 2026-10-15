package presentation.controller;

import javafx.concurrent.Task;
import presentation.Presenter;
import presentation.util.DialogHelper;
import presentation.util.NavigationManager;

import java.util.concurrent.Callable;
import java.util.function.Consumer;

public abstract class FxController {
    protected NavigationManager navigation;
    protected Presenter presenter;

    protected void connect(NavigationManager navigation) {
        this.navigation = navigation;
        this.presenter = navigation.getPresenter();
    }

    protected <T> void background(Callable<T> operation, Consumer<T> success) {
        background(operation, success, this::showError);
    }

    protected <T> void background(Callable<T> operation, Consumer<T> success,
                                  Consumer<Throwable> failure) {
        Task<T> task = new Task<>() {
            @Override
            protected T call() throws Exception {
                return operation.call();
            }
        };
        task.setOnSucceeded(event -> success.accept(task.getValue()));
        task.setOnFailed(event -> failure.accept(task.getException()));
        navigation.execute(task);
    }

    protected void showError(Throwable error) {
        error.printStackTrace();
        DialogHelper.showError(navigation.getPrimaryStage(), "Ошибка",
                error.getMessage() == null ? "Не удалось выполнить операцию" : error.getMessage());
    }
}

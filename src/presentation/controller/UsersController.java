package presentation.controller;

import domain.model.User;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import presentation.util.DialogHelper;
import presentation.util.NavigationManager;

import java.util.Locale;

public class UsersController extends FxController {
    @FXML private TextField searchField;
    @FXML private Button addUserBtn, backBtn, editUserBtn, deleteUserBtn, exportUsersBtn, refreshUsersBtn;
    @FXML private Label countLabel;
    @FXML private TableView<User> userTable;
    @FXML private TableColumn<User, Integer> idColumn;
    @FXML private TableColumn<User, String> usernameColumn, emailColumn, roleColumn;
    private final ObservableList<User> users = FXCollections.observableArrayList();
    private final FilteredList<User> filtered = new FilteredList<>(users);

    public void init(NavigationManager navigation) {
        connect(navigation);
        idColumn.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(cell.getValue().getId()));
        usernameColumn.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().getUsername()));
        emailColumn.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().getEmail()));
        roleColumn.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().getRole().name()));
        SortedList<User> sorted = new SortedList<>(filtered);
        sorted.comparatorProperty().bind(userTable.comparatorProperty());
        userTable.setItems(sorted);
        userTable.setPlaceholder(new Label("Пользователи не найдены"));
        searchField.textProperty().addListener((obs, old, value) -> filter());
        backBtn.setOnAction(event -> navigation.showCatalog());
        addUserBtn.setOnAction(event -> editUser(null));
        editUserBtn.disableProperty().bind(userTable.getSelectionModel().selectedItemProperty().isNull());
        deleteUserBtn.disableProperty().bind(userTable.getSelectionModel().selectedItemProperty().isNull());
        editUserBtn.setOnAction(event -> editUser(userTable.getSelectionModel().getSelectedItem()));
        deleteUserBtn.setOnAction(event -> deleteUser());
        exportUsersBtn.setOnAction(event -> navigation.exportUsers());
        refreshUsersBtn.setOnAction(event -> reload());
        reload();
    }

    private void reload() {
        refreshUsersBtn.setDisable(true);
        countLabel.setText("Загрузка…");
        background(presenter::onGetUsers, result -> {
            users.setAll(result);
            refreshUsersBtn.setDisable(false);
            filter();
        }, error -> {
            refreshUsersBtn.setDisable(false);
            countLabel.setText("Ошибка загрузки");
            navigation.setDatabaseError();
            showError(error);
        });
    }

    private void filter() {
        String query = searchField.getText().strip().toLowerCase(Locale.ROOT);
        filtered.setPredicate(user -> (user.getUsername() + " " + user.getEmail()).toLowerCase(Locale.ROOT).contains(query));
        countLabel.setText("Пользователей: " + filtered.size() + " из " + users.size());
    }

    private void deleteUser() {
        User user = userTable.getSelectionModel().getSelectedItem();
        if (user == null || !DialogHelper.showConfirmation(navigation.getPrimaryStage(), "Удаление пользователя",
                "Удалить «" + user.getUsername() + "»?", "Это действие нельзя отменить.")) return;
        userTable.setDisable(true);
        background(() -> { presenter.onDeleteUser(user.getId()); return true; }, result -> {
            userTable.setDisable(false);
            reload();
        }, error -> {
            userTable.setDisable(false);
            showError(error);
        });
    }

    private void editUser(User user) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.initOwner(navigation.getPrimaryStage());
        dialog.setTitle(user == null ? "Добавить пользователя" : "Редактировать пользователя");
        TextField username = new TextField(user == null ? "" : user.getUsername());
        TextField email = new TextField(user == null ? "" : user.getEmail());
        PasswordField hash = new PasswordField();
        hash.setPromptText(user == null ? "Введите готовый хеш пароля" : "Оставьте пустым, чтобы сохранить хеш");
        ComboBox<User.Role> role = new ComboBox<>(FXCollections.observableArrayList(User.Role.values()));
        role.setValue(user == null ? User.Role.AUTHOR : user.getRole());
        Label error = new Label();
        error.setWrapText(true);
        GridPane form = new GridPane();
        form.setHgap(12);
        form.setVgap(12);
        form.setPadding(new Insets(20));
        form.addRow(0, new Label("Имя"), username);
        form.addRow(1, new Label("Email"), email);
        form.addRow(2, new Label("Хеш пароля"), hash);
        form.addRow(3, new Label("Роль"), role);
        form.add(error, 0, 4, 2, 1);
        dialog.getDialogPane().setContent(form);
        ButtonType saveType = new ButtonType("Сохранить", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveType, ButtonType.CANCEL);
        Button save = (Button) dialog.getDialogPane().lookupButton(saveType);
        Button cancel = (Button) dialog.getDialogPane().lookupButton(ButtonType.CANCEL);
        boolean[] saving = {false};
        dialog.setOnCloseRequest(event -> { if (saving[0]) event.consume(); });
        save.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            event.consume();
            if (saving[0]) return;
            String nameValue = username.getText().strip();
            String emailValue = email.getText().strip();
            String hashValue = hash.getText().isBlank() && user != null ? user.getPasswordHash() : hash.getText();
            User.Role roleValue = role.getValue();
            saving[0] = true;
            save.setDisable(true);
            cancel.setDisable(true);
            error.setText("Сохранение…");
            background(() -> {
                if (user == null) presenter.onAddUser(new User(0, nameValue, emailValue, hashValue, roleValue));
                else presenter.onEditUser(user.getId(), nameValue, emailValue, hashValue, roleValue);
                return true;
            }, result -> {
                saving[0] = false;
                dialog.close();
                reload();
            }, failure -> {
                saving[0] = false;
                save.setDisable(false);
                cancel.setDisable(false);
                error.setText(failure.getMessage());
            });
        });
        dialog.showAndWait();
    }
}

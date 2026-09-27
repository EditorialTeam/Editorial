package data.local.repository;

import domain.model.User;
import domain.repository.UserRepository;
import data.local.database.DatabaseConnectionFactory;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

// Класс для работы с БД пользователей в Java коде
public class JdbcUserRepository implements UserRepository {
    private final DatabaseConnectionFactory connectionFactory;

    // Конструктор класса
    public JdbcUserRepository(DatabaseConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    // Метод добавления пользователя
    @Override
    public void addUser(User user) {
        // sql запрос: вставляем пользователя с поиском id роли по коду
        String sql = """
                INSERT INTO users (username, email, password_hash, role_id)
                VALUES (?, ?, ?, (SELECT id FROM roles WHERE code = ?))
                RETURNING id
                """; // После вставки PostgreSQL возвращает сгенерированный ключ

        try (var connection = connectionFactory.openConnection(); // Открываем соединение
             var statement = connection.prepareStatement(sql);    // Готовим запрос
        ) {
            // Задаем значения параметрам запроса
            statement.setString(1, user.getUsername());
            statement.setString(2, user.getEmail());
            statement.setString(3, user.getPasswordHash());
            statement.setString(4, user.getRole().name());

            // Пытаемся выполнить sql запрос
            try (var res = statement.executeQuery()) {
                if (!res.next()) throw new IllegalStateException("Database didn't return anything"); // Если не вернули id

                user.setId(res.getInt("id")); // Ставим id возвращенное PostgreSQL
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Couldn't add user", e); // Если возникла какая-нибудь ошибка при добавлении пользователя возвращаем ошибку
        }
    }

    // Метод редактирования пользователя по объекту пользователя
    @Override
    public void editUser(User user) {
        // sql запрос: изменяем данные пользователя по id
        String sql = """
                UPDATE users 
                SET
                    username = ?,
                    email = ?,
                    password_hash = ?,
                    role_id = (SELECT id FROM roles WHERE code = ?)
                WHERE id = ?
                """;

        try (
                var connection = connectionFactory.openConnection(); // Открываем соединение
                var statement = connection.prepareStatement(sql)     // Готовим запрос
        ) {
            // Задаем значения параметрам запроса
            statement.setString(1, user.getUsername());
            statement.setString(2, user.getEmail());
            statement.setString(3, user.getPasswordHash());
            statement.setString(4, user.getRole().name());
            statement.setInt(5, user.getId());

            // Получаем кол-во измененных пользователей и выполняем запрос
            int affectedRows = statement.executeUpdate();

            if (affectedRows == 0) throw new IllegalArgumentException("Couldn't find the user"); // Если не удалось изменить пользователя возвращаем ошибку
        } catch (SQLException e) {
            throw new IllegalStateException("Couldn't edit the user"); // Если возникла какая-нибудь ошибка при изменении пользователя возвращаем ошибку
        }
    }

    // Метод удаления пользователя по id
    @Override
    public void deleteUser(int userId) {
        // sql запрос: удаляем пользователя по id
        String sql = """
                DELETE FROM users WHERE id = ?
                """;

        try (var connection = connectionFactory.openConnection(); // Открываем соединение
             var statement = connection.prepareStatement(sql);    // Готовим запрос
        ) {
            // Задаем значения параметрам запроса
            statement.setInt(1, userId);

            // Получаем кол-во удаленных пользователей и выполняем запрос
            int affectedRows = statement.executeUpdate();

            if (affectedRows == 0) throw new IllegalArgumentException("Couldn't find such user"); // Если не удалось найти пользователя для удаления возвращаем ошибку

        } catch (SQLException e) {
            throw new IllegalStateException("Couldn't delete user", e); // Если возникла какая-нибудь ошибка при удалении пользователя возвращаем ошибку
        }
    }

    // Функция для получения конкретного пользователя по id
    @Override
    public User getUserById(int userId) {
        // sql запрос: возвращаем данные о пользователе по id вместе с кодом роли
        String sql = """
                SELECT
                    users.id,
                    users.username,
                    users.email,
                    users.password_hash,
                    roles.code AS role_code
                FROM users
                JOIN roles ON roles.id = users.role_id
                WHERE users.id = ?
                """;

        try (var connection = connectionFactory.openConnection(); // Открываем соединение
             var statement = connection.prepareStatement(sql);    // Готовим запрос
        ) {
            // Задаем значения параметрам запроса
            statement.setInt(1, userId);

            // Получаем пользователя по id и выполняем запрос
            try (var resSet = statement.executeQuery()) {
                if (!resSet.next()) throw new IllegalStateException("Database didn't return user"); // Если не удалось найти пользователя возвращаем ошибку

                // Создаем объект класса пользователя в коде из полученных данных
                User returnUser = new User(resSet.getInt("id"),
                        resSet.getString("username"),
                        resSet.getString("email"),
                        resSet.getString("password_hash"),
                        User.Role.valueOf(resSet.getString("role_code")));
                return returnUser; // Возвращаем пользователя
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Couldn't get user", e); // Если возникла какая-нибудь ошибка при поиске пользователя возвращаем ошибку
        }
    }

    // Метод проверки существования пользователя по id
    @Override
    public boolean existsById(int userId) {
        // sql запрос: проверяем наличие записи без выгрузки всех полей
        String sql = """
                SELECT 1
                FROM users
                WHERE id = ?
                """;

        try (
                var connection = connectionFactory.openConnection(); // Открываем соединение
                var statement = connection.prepareStatement(sql)     // Готовим запрос
        ) {
            // Задаем значения параметрам запроса
            statement.setInt(1, userId);

            // Выполняем запрос
            try (var result = statement.executeQuery()) {
                return result.next(); // Возвращаем true, если строка найдена (пользователь существует), иначе false
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Couldn't check if user exists", e); // Если возникла какая-нибудь ошибка при проверке пользователя возвращаем ошибку
        }
    }

    @Override
    public List<User> getUsers() {
        String sql = """
                SELECT
                    users.id,
                    users.username,
                    users.email,
                    users.password_hash,
                    roles.code AS role_code
                FROM users
                JOIN roles ON roles.id = users.role_id
                """;

        try (var connection = connectionFactory.openConnection();
             var statement = connection.prepareStatement(sql)
        ) {

            try (var resSet = statement.executeQuery()) {
                List<User> returnUsersList = new ArrayList<>();

                while (resSet.next()) {
                    returnUsersList.add(new User(
                            resSet.getInt("id"),
                            resSet.getString("username"),
                            resSet.getString("email"),
                            resSet.getString("password_hash"),
                            User.Role.valueOf(resSet.getString("role_code"))
                    ));
                }

                return returnUsersList;
            }
        }
        catch (SQLException e) {
            throw new IllegalStateException("Couldn't get users", e);
        }

    }
}

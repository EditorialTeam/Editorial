package data.local.database;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

public class DatabaseConfig {
    private static final String URL_PROPERTY = "db.url";
    private static final String USER_PROPERTY = "db.user";
    private static final String PASSWORD_PROPERTY = "db.password";

    private static final String URL_ENV = "DB_URL";
    private static final String USER_ENV = "DB_USER";
    private static final String PASSWORD_ENV = "DB_PASSWORD";

    private final String url;
    private final String user;
    private final String password;

    // Конструктор класса через заданные параметры в теле конструктора
    public DatabaseConfig(String url, String user, String password) {
        this.url = requireValue(url, URL_ENV, URL_PROPERTY);
        this.user = requireValue(user, USER_ENV, USER_PROPERTY);
        this.password = requireValue(password, PASSWORD_ENV, PASSWORD_PROPERTY);
    }

    // Конструктор класса через конфиг database.properties
    public DatabaseConfig() {
        Properties properties = loadProperties();
        this.url = resolve(URL_ENV, URL_PROPERTY, properties);
        this.user = resolve(USER_ENV, USER_PROPERTY, properties);
        this.password = resolve(PASSWORD_ENV, PASSWORD_PROPERTY, properties);
    }

    // Геттеры
    public String getUrl() {
        return url;
    }

    public String getUser() {
        return user;
    }

    public String getPassword() {
        return password;
    }

    private static String resolve(String envName, String propertyKey, Properties properties) {
        String fromEnv = System.getenv(envName);
        if (fromEnv != null && !fromEnv.isBlank()) {
            return fromEnv;
        }

        String fromFile = properties.getProperty(propertyKey);
        if (fromFile != null && !fromFile.isBlank()) {
            return fromFile;
        }

        throw new IllegalStateException(
                "No database setting: set environment variable " + envName
                        + " or property " + propertyKey
        );
    }

    private static String requireValue(String value, String envName, String propertyKey) {
        if (value == null || value.isBlank()) {
            throw new IllegalStateException(
                    "No database setting: set environment variable " + envName
                            + " or property " + propertyKey
            );
        }
        return value;
    }

    private static Properties loadProperties() {
        Properties properties = new Properties();
        try (var stream = DatabaseConfig.class.getResourceAsStream("/database.properties")) {
            // Если ничего не получаем - возвращаем properties
            if (stream == null) {
                return properties;
            }

            // Пытаемся прочитать данные и занести их в properties
            try (var reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
                properties.load(reader);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Couldn't read database.properties", e); // Возвращаем ошибку
        }
        return properties;
    }
}
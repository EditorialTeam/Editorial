package data.local.database;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

public class DatabaseConfig {
    private final Properties properties = new Properties();

    public DatabaseConfig(String url, String user, String password) {
        properties.setProperty("db.url", url);
        properties.setProperty("db.user", user);
        properties.setProperty("db.password", password);
    }

    public DatabaseConfig() {
        try (var stream = DatabaseConfig.class.getResourceAsStream("/database.properties")) {
            if (stream == null) {
                throw new IllegalStateException("No database.properties file");
            }

            try (var reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
                properties.load(reader);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Couldn't read database.properties", e);
        }
    }

    public String getUrl() {
        return getRequired("db.url");
    }

    public String getUser() {
        return getRequired("db.user");
    }

    public String getPassword() {
        return getRequired("db.password");
    }

    private String getRequired(String key) {
        String value = properties.getProperty(key);

        if (value == null || value.isBlank()) {
            throw new IllegalStateException("No parameter set in database.properties: " + key);
        }

        return value;
    }
}

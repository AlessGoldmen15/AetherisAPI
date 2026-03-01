package fr.aetheris.api.storage.jdbc;

import fr.aetheris.api.storage.KeyValueStore;
import fr.aetheris.api.storage.StorageException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public final class JdbcKeyValueStore implements KeyValueStore {

    private final String jdbcUrl;
    private final String username;
    private final String password;
    private final String tableName;
    private final String namespace;

    public JdbcKeyValueStore(String jdbcUrl, String username, String password, String tableName, String namespace) {
        this.jdbcUrl = jdbcUrl;
        this.username = username;
        this.password = password;
        this.tableName = tableName;
        this.namespace = namespace;
        initializeSchema();
    }

    @Override
    public void put(String key, String value) {
        final String sql = "INSERT INTO " + tableName + " (namespace, storage_key, storage_value) VALUES (?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE storage_value = VALUES(storage_value)";
        try (Connection connection = JdbcStorageProvider.createConnection(jdbcUrl, username, password);
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, namespace);
            statement.setString(2, key);
            statement.setString(3, value);
            statement.executeUpdate();
        } catch (SQLException exception) {
            throw new StorageException("Unable to put value in JDBC storage", exception);
        }
    }

    @Override
    public Optional<String> get(String key) {
        final String sql = "SELECT storage_value FROM " + tableName + " WHERE namespace = ? AND storage_key = ?";
        try (Connection connection = JdbcStorageProvider.createConnection(jdbcUrl, username, password);
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, namespace);
            statement.setString(2, key);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.ofNullable(resultSet.getString("storage_value"));
                }
                return Optional.empty();
            }
        } catch (SQLException exception) {
            throw new StorageException("Unable to get value from JDBC storage", exception);
        }
    }

    @Override
    public void delete(String key) {
        final String sql = "DELETE FROM " + tableName + " WHERE namespace = ? AND storage_key = ?";
        try (Connection connection = JdbcStorageProvider.createConnection(jdbcUrl, username, password);
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, namespace);
            statement.setString(2, key);
            statement.executeUpdate();
        } catch (SQLException exception) {
            throw new StorageException("Unable to delete value from JDBC storage", exception);
        }
    }

    @Override
    public Set<String> keys() {
        final String sql = "SELECT storage_key FROM " + tableName + " WHERE namespace = ?";
        final Set<String> keys = new HashSet<>();
        try (Connection connection = JdbcStorageProvider.createConnection(jdbcUrl, username, password);
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, namespace);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    keys.add(resultSet.getString("storage_key"));
                }
            }
            return keys;
        } catch (SQLException exception) {
            throw new StorageException("Unable to list keys from JDBC storage", exception);
        }
    }

    @Override
    public void close() {
    }

    private void initializeSchema() {
        final String sql = "CREATE TABLE IF NOT EXISTS " + tableName + " (" +
                "namespace VARCHAR(128) NOT NULL, " +
                "storage_key VARCHAR(255) NOT NULL, " +
                "storage_value TEXT, " +
                "PRIMARY KEY(namespace, storage_key)" +
                ")";
        try (Connection connection = JdbcStorageProvider.createConnection(jdbcUrl, username, password);
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.executeUpdate();
        } catch (SQLException exception) {
            throw new StorageException("Unable to initialize JDBC storage schema", exception);
        }
    }
}

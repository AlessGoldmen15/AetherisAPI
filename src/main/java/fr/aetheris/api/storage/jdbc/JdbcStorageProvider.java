package fr.aetheris.api.storage.jdbc;

import fr.aetheris.api.storage.KeyValueStore;
import fr.aetheris.api.storage.StorageProvider;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class JdbcStorageProvider implements StorageProvider {

    private final String jdbcUrl;
    private final String username;
    private final String password;
    private final String tableName;

    public JdbcStorageProvider(String jdbcUrl, String username, String password, String tableName) {
        this.jdbcUrl = jdbcUrl;
        this.username = username;
        this.password = password;
        this.tableName = tableName;
    }

    @Override
    public KeyValueStore open(String namespace) {
        return new JdbcKeyValueStore(jdbcUrl, username, password, tableName, namespace);
    }

    @Override
    public void close() {
    }

    static Connection createConnection(String jdbcUrl, String username, String password) throws SQLException {
        return DriverManager.getConnection(jdbcUrl, username, password);
    }
}

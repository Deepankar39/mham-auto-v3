package com.mhamauto.db;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.SQLException;

/** Lazy pool: the Phase 1.3 homepage and static catalogue work even before DB setup. */
public final class DbPool {
    private static volatile HikariDataSource pool;
    private DbPool() { }

    public static Connection getConnection() throws SQLException {
        return source().getConnection();
    }

    private static HikariDataSource source() {
        HikariDataSource current = pool;
        if (current == null) {
            synchronized (DbPool.class) {
                current = pool;
                if (current == null) {
                    DbConfig config = DbConfig.fromEnvironment();
                    HikariConfig hikari = new HikariConfig();
                    hikari.setJdbcUrl(config.url());
                    hikari.setDriverClassName("com.mysql.cj.jdbc.Driver");
                    hikari.setUsername(config.username());
                    hikari.setPassword(config.password());
                    hikari.setPoolName("mham-academic-mysql");
                    hikari.setMaximumPoolSize(5);
                    hikari.setMinimumIdle(0);
                    hikari.setConnectionTimeout(5000);
                    hikari.setInitializationFailTimeout(-1);
                    hikari.setAutoCommit(true);
                    current = new HikariDataSource(hikari);
                    pool = current;
                }
            }
        }
        return current;
    }

    public static synchronized void shutdown() {
        if (pool != null) {
            pool.close();
            pool = null;
        }
    }
}

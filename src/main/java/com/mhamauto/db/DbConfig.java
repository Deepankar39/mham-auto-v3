package com.mhamauto.db;

import java.util.Map;

/** Credentials come from local environment, never from the WAR or source tree. */
public record DbConfig(String url, String username, String password) {
    public static DbConfig fromEnvironment() {
        return fromMap(System.getenv());
    }

    public static DbConfig fromMap(Map<String, String> env) {
        String url = env.getOrDefault("MHAM_DB_URL",
                "jdbc:mysql://127.0.0.1:3306/mham_auto?sslMode=DISABLED&allowPublicKeyRetrieval=true&connectionTimeZone=UTC");
        String user = env.getOrDefault("MHAM_DB_USER", "").trim();
        String password = env.get("MHAM_DB_PASSWORD");
        if (!url.startsWith("jdbc:mysql:") || !url.contains("/mham_auto")) {
            throw new IllegalStateException("MHAM_DB_URL must be a MySQL URL for the mham_auto schema");
        }
        if (user.isEmpty() || password == null || password.isEmpty()) {
            throw new IllegalStateException("Set MHAM_DB_USER and MHAM_DB_PASSWORD environment variables");
        }
        return new DbConfig(url, user, password);
    }
}

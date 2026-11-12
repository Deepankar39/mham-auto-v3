package com.mhamauto.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/** Small immutable non-secret configuration contract for the academic version. */
public record AppSettings(String market, String currency, boolean demoMode) {
    public static final String CONTEXT_ATTRIBUTE = AppSettings.class.getName();

    public static AppSettings load() {
        Properties properties = new Properties();
        try (InputStream in = AppSettings.class.getResourceAsStream("/mham-auto.properties")) {
            if (in == null) {
                throw new IllegalStateException("Missing mham-auto.properties on classpath");
            }
            properties.load(in);
        } catch (IOException e) {
            throw new IllegalStateException("Could not read application settings", e);
        }
        String market = properties.getProperty("app.market", "").trim();
        String currency = properties.getProperty("app.currency", "").trim();
        String demo = properties.getProperty("app.demoMode", "").trim();
        if (!"KW".equals(market) || !"KWD".equals(currency)) {
            throw new IllegalStateException("Academic V1 must use market KW and currency KWD");
        }
        if (!"true".equalsIgnoreCase(demo)) {
            throw new IllegalStateException("Academic V1 must remain in demo mode");
        }
        return new AppSettings(market, currency, true);
    }
}

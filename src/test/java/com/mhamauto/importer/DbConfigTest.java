package com.mhamauto.importer;

import static org.junit.jupiter.api.Assertions.*;
import com.mhamauto.db.DbConfig;
import java.util.Map;
import org.junit.jupiter.api.Test;

class DbConfigTest {
    @Test void needsExplicitCredentials() {
        assertThrows(IllegalStateException.class,()->DbConfig.fromMap(Map.of()));
        var settings=DbConfig.fromMap(Map.of("MHAM_DB_USER","mham_app","MHAM_DB_PASSWORD","test-pass"));
        assertTrue(settings.url().contains("mham_auto"));
        assertEquals("mham_app",settings.username());
    }
}

package com.mhamauto.auth;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class IdentityRulesTest {
    @Test void emailCaseAndSyntax() {
        assertEquals("customer@example.com",IdentityRules.email("  Customer@Example.COM "));
        assertThrows(IllegalArgumentException.class,()->IdentityRules.email("not-an-email"));
        assertThrows(IllegalArgumentException.class,()->IdentityRules.email("a@"));
    }
    @Test void nameAndPasswordLimits() {
        assertEquals("Customer One",IdentityRules.displayName(" Customer One "));
        assertThrows(IllegalArgumentException.class,()->IdentityRules.displayName("x"));
        assertThrows(IllegalArgumentException.class,()->IdentityRules.displayName("Bad\nName"));
        assertThrows(IllegalArgumentException.class,()->IdentityRules.password("short"));
        assertDoesNotThrow(()->IdentityRules.password("A-longer-example-passphrase-123"));
    }
}

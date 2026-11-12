package com.mhamauto.auth;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PasswordHasherTest {
    @Test void saltedVersionedHashesVerify() {
        String secret="Test-only-long-password-123";
        String h1=PasswordHasher.hash(secret);
        String h2=PasswordHasher.hash(secret);
        assertNotEquals(h1,h2,"Every registration needs a fresh salt");
        assertTrue(PasswordHasher.verify(secret,h1));
        assertFalse(PasswordHasher.verify("invalid-password",h1));
        assertFalse(PasswordHasher.verify(secret,"plain-text"));
        assertFalse(PasswordHasher.verify(secret,null));
    }
}

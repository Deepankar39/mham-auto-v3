package com.mhamauto.web;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class RequestSecurityTest {
    @Test void localOrigin() {
        assertTrue(RequestSecurity.sameOrigin("http://localhost:8080", "http", "localhost", 8080));
        assertTrue(RequestSecurity.sameOrigin(null, "http", "localhost", 8080));
        assertTrue(RequestSecurity.sameOrigin("", "http", "localhost", 8080));
        assertTrue(RequestSecurity.sameOrigin("https://example.com", "https", "example.com", 443));
        assertTrue(RequestSecurity.sameOrigin("https://EXAMPLE.COM:443", "https", "example.com", 443));
    }
    @Test void invalidOrigin() {
        assertFalse(RequestSecurity.sameOrigin("null", "http", "localhost", 8080));
        assertFalse(RequestSecurity.sameOrigin("http://evil.example", "http", "localhost", 8080));
        assertFalse(RequestSecurity.sameOrigin("https://localhost:8080", "http", "localhost", 8080));
        assertFalse(RequestSecurity.sameOrigin("http://localhost:9090", "http", "localhost", 8080));
        assertFalse(RequestSecurity.sameOrigin("http://localhost:8080/path", "http", "localhost", 8080));
        assertFalse(RequestSecurity.sameOrigin("http://user@localhost:8080", "http", "localhost", 8080));
        assertFalse(RequestSecurity.sameOrigin("javascript:alert(1)", "http", "localhost", 8080));
    }
    @Test void acceptedMethodsAndPolicy() {
        assertFalse(RequestSecurity.isUnsafeMethod("GET"));
        assertFalse(RequestSecurity.isUnsafeMethod("HEAD"));
        assertTrue(RequestSecurity.isUnsafeMethod("POST"));
        assertTrue(RequestSecurity.isUnsafeMethod("DELETE"));
        assertTrue(RequestSecurity.CONTENT_SECURITY_POLICY.contains("frame-ancestors 'none'"));
        assertTrue(RequestSecurity.CONTENT_SECURITY_POLICY.contains("https://cdn.shopify.com"));
        assertFalse(RequestSecurity.CONTENT_SECURITY_POLICY.contains("'unsafe-inline'"));
    }
}

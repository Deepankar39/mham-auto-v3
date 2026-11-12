import com.mhamauto.web.RequestSecurity;
public final class Phase10OfflineCheck {
    private static void assertThat(boolean b, String message) { if (!b) throw new AssertionError(message); }
    public static void main(String[] args) {
        assertThat(RequestSecurity.sameOrigin("http://localhost:8080", "http", "localhost", 8080), "Same origin");
        assertThat(RequestSecurity.sameOrigin(null, "http", "localhost", 8080), "Missing origin allowed; CSRF still required");
        assertThat(!RequestSecurity.sameOrigin("null", "http", "localhost", 8080), "Reject null origin");
        assertThat(!RequestSecurity.sameOrigin("http://localhost:8080/a", "http", "localhost", 8080), "Reject paths");
        assertThat(!RequestSecurity.sameOrigin("http://malicious.example", "http", "localhost", 8080), "Reject cross origin");
        assertThat(!RequestSecurity.sameOrigin("https://localhost:8080", "http", "localhost", 8080), "Reject wrong scheme");
        assertThat(!RequestSecurity.sameOrigin("http://localhost:9090", "http", "localhost", 8080), "Reject wrong port");
        assertThat(RequestSecurity.CONTENT_SECURITY_POLICY.contains("https://cdn.shopify.com"), "Shopify images allowed");
        assertThat(!RequestSecurity.CONTENT_SECURITY_POLICY.contains("'unsafe-inline'"), "No inline scripts or styles");
        System.out.println("PASS - Phase 10 HTTP origin and policy rules");
    }
}

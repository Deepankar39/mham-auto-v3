package com.mhamauto.web;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Locale;

/** Browser-origin check complements, but never replaces, the existing session CSRF tokens. */
public final class RequestSecurity {
    public static final String CONTENT_SECURITY_POLICY =
            "default-src 'self'; base-uri 'self'; object-src 'none'; frame-ancestors 'none'; " +
            "script-src 'self'; style-src 'self'; img-src 'self' https://cdn.shopify.com; " +
            "connect-src 'self'; font-src 'self'; form-action 'self'";

    private RequestSecurity() { }

    public static boolean isUnsafeMethod(String method) {
        return !"GET".equalsIgnoreCase(method) && !"HEAD".equalsIgnoreCase(method)
                && !"OPTIONS".equalsIgnoreCase(method);
    }

    /** Missing Origin is allowed for legacy form clients; servlet CSRF remains mandatory. */
    public static boolean sameOrigin(String origin, String requestScheme, String host, int port) {
        if (origin == null || origin.isBlank()) return true;
        if (requestScheme == null || host == null || port < 1 || port > 65535) return false;
        try {
            URI uri = new URI(origin);
            String scheme = uri.getScheme();
            String sourceHost = uri.getHost();
            if (scheme == null || sourceHost == null || uri.getRawUserInfo() != null
                    || uri.getRawQuery() != null || uri.getRawFragment() != null
                    || (uri.getRawPath() != null && !uri.getRawPath().isEmpty())
                    || !("http".equalsIgnoreCase(scheme) || "https".equalsIgnoreCase(scheme))) {
                return false;
            }
            int sourcePort = uri.getPort() == -1
                    ? ("https".equalsIgnoreCase(scheme) ? 443 : 80) : uri.getPort();
            return scheme.equalsIgnoreCase(requestScheme)
                    && sourceHost.toLowerCase(Locale.ROOT).equals(host.toLowerCase(Locale.ROOT))
                    && sourcePort == port;
        } catch (URISyntaxException ex) {
            return false;
        }
    }
}

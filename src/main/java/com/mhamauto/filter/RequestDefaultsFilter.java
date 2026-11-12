package com.mhamauto.filter;

import com.mhamauto.web.RequestSecurity;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/** Shared HTTP hardening: strict content types, local form origins, asset caching, CSP. */
@WebFilter(urlPatterns = "/*")
public final class RequestDefaultsFilter implements Filter {
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        if (request.getCharacterEncoding() == null) {
            request.setCharacterEncoding(StandardCharsets.UTF_8.name());
        }
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        if (request instanceof HttpServletRequest req && response instanceof HttpServletResponse http) {
            http.setHeader("X-Content-Type-Options", "nosniff");
            http.setHeader("X-Frame-Options", "DENY");
            http.setHeader("Referrer-Policy", "strict-origin-when-cross-origin");
            http.setHeader("X-Permitted-Cross-Domain-Policies", "none");
            http.setHeader("Permissions-Policy", "camera=(), microphone=(), geolocation=(), payment=()");
            http.setHeader("Content-Security-Policy", RequestSecurity.CONTENT_SECURITY_POLICY);

            if ("TRACE".equalsIgnoreCase(req.getMethod())) {
                http.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
                return;
            }
            if (RequestSecurity.isUnsafeMethod(req.getMethod()) && !RequestSecurity.sameOrigin(
                    req.getHeader("Origin"), req.getScheme(), req.getServerName(), req.getServerPort())) {
                http.sendError(HttpServletResponse.SC_FORBIDDEN, "Cross-origin form submission denied");
                return;
            }
            // Local assets are not fingerprinted; short cache + revalidation avoids stale CSS after overlays.
            if (("GET".equalsIgnoreCase(req.getMethod()) || "HEAD".equalsIgnoreCase(req.getMethod()))
                    && req.getServletPath().startsWith("/assets/")) {
                http.setHeader("Cache-Control", "public, max-age=300, must-revalidate");
            }
        }
        chain.doFilter(request, response);
    }
}

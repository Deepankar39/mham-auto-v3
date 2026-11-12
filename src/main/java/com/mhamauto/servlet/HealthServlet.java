package com.mhamauto.servlet;

import com.mhamauto.config.AppSettings;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/** Liveness only: success does not imply a future MySQL connection is healthy. */
@WebServlet(name = "HealthServlet", urlPatterns = "/health")
public final class HealthServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Object value = getServletContext().getAttribute(AppSettings.CONTEXT_ATTRIBUTE);
        if (!(value instanceof AppSettings settings) || !settings.demoMode()) {
            response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
            return;
        }
        response.setHeader("Cache-Control", "no-store");
        response.setContentType("text/plain;charset=UTF-8");
        response.getWriter().println("OK - MHAM AUTO Academic V1");
    }
}

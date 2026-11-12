package com.mhamauto.servlet;

import com.mhamauto.db.DbPool;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/** Readiness only. Never expose JDBC URL, usernames, exceptions, or counts publicly. */
@WebServlet(name = "DatabaseStatusServlet", urlPatterns = "/api/db-status")
public final class DatabaseStatusServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    @Override protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setHeader("Cache-Control", "no-store");
        response.setContentType("text/plain;charset=UTF-8");
        try (Connection connection = DbPool.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT 1");
             ResultSet result = statement.executeQuery()) {
            if (!result.next() || result.getInt(1) != 1) throw new IllegalStateException("No database response");
            response.getWriter().println("DB READY - MHAM AUTO Academic V1");
        } catch (Exception notReady) {
            getServletContext().log("Database readiness check failed", notReady);
            response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
            response.getWriter().println("DB UNAVAILABLE - check local Phase 2 setup");
        }
    }
}

package com.mhamauto.servlet;

import com.mhamauto.model.CatalogPreviewCategory;
import com.mhamauto.service.CatalogPreviewService;
import com.mhamauto.web.ViewPaths;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/** GET /catalog demonstrates Browser -> Servlet -> Service -> JSP. */
@WebServlet(name = "CatalogPreviewServlet", urlPatterns = "/catalog")
public final class CatalogPreviewServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private final CatalogPreviewService service = new CatalogPreviewService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        final CatalogPreviewService.Audience audience;
        try {
            audience = service.parseAudience(request.getParameter("audience"));
        } catch (IllegalArgumentException invalidSelection) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid catalogue selection");
            return;
        }

        List<CatalogPreviewCategory> categories = service.getCategories();
        request.setAttribute("audience", audience);
        // For this first JSP stage, six explicitly named attributes avoid introducing JSTL dependencies.
        // A database-driven catalogue and reusable rendering are scheduled for a later phase.
        for (int i = 0; i < categories.size(); i++) {
            request.setAttribute("category" + i, categories.get(i));
        }
        response.setContentType("text/html;charset=UTF-8");
        request.getRequestDispatcher(ViewPaths.CATALOG_PREVIEW).forward(request, response);
    }
}

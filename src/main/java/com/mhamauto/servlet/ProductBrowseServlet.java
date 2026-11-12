package com.mhamauto.servlet;

import com.mhamauto.catalog.CatalogDao;
import com.mhamauto.catalog.CatalogQuery;
import com.mhamauto.db.DbPool;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

/** Phase 3: always read actual MySQL records, never the old preview data. */
@WebServlet(name="ProductBrowseServlet", urlPatterns="/products")
public final class ProductBrowseServlet extends HttpServlet {
    private final CatalogDao dao = new CatalogDao();
    @Override protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        CatalogQuery query;
        try {
            query=CatalogQuery.parse(request.getParameter("q"),request.getParameter("vendor"),
                request.getParameter("category"),request.getParameter("sort"),request.getParameter("page"));
        } catch (IllegalArgumentException invalid) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST,"Invalid catalogue query parameters");
            return;
        }
        response.setHeader("Cache-Control","no-store"); // Imported prices/stock may be updated later.
        try (Connection c=DbPool.getConnection()) {
            request.setAttribute("filter",query);
            request.setAttribute("listing",dao.search(c,query));
            request.setAttribute("vendors",dao.vendors(c));
            request.setAttribute("categories",dao.categories(c));
            request.getRequestDispatcher("/WEB-INF/views/products.jsp").forward(request,response);
        } catch (SQLException | IllegalStateException e) {
            getServletContext().log("Phase 3 catalogue read failed",e);
            response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE,"Catalogue temporarily unavailable");
        }
    }
}

package com.mhamauto.servlet;

import com.mhamauto.catalog.CatalogDao;
import com.mhamauto.catalog.CatalogModels.Detail;
import com.mhamauto.db.DbPool;
import com.mhamauto.auth.Csrf;
import com.mhamauto.auth.SessionAuth;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

@WebServlet(name="ProductDetailServlet",urlPatterns="/product")
public final class ProductDetailServlet extends HttpServlet {
    private final CatalogDao dao=new CatalogDao();
    @Override protected void doGet(HttpServletRequest request,HttpServletResponse response)
            throws ServletException, IOException {
        String handle=request.getParameter("handle");
        if (handle==null || handle.isBlank() || handle.length()>255 || handle.indexOf('/')>=0) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST,"Invalid product handle");return;
        }
        response.setHeader("Cache-Control","no-store");
        try(Connection conn=DbPool.getConnection()) {
            Detail detail=dao.detail(conn,handle);
            if(detail==null) { response.sendError(HttpServletResponse.SC_NOT_FOUND,"Product not found");return; }
            request.setAttribute("product",detail);
            if(SessionAuth.principal(request)!=null) request.setAttribute("cartCsrf",Csrf.issue(request));
            request.getRequestDispatcher("/WEB-INF/views/product-detail.jsp").forward(request,response);
        } catch (SQLException | IllegalStateException e) {
            getServletContext().log("Phase 3 product detail read failed",e);
            response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE,"Product details temporarily unavailable");
        }
    }
}

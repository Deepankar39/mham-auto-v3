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

/** Progressive enhancement: the homepage remains usable without database access. */
@WebServlet(name="FeaturedProductsServlet",urlPatterns="/featured")
public final class FeaturedProductsServlet extends HttpServlet {
  private final CatalogDao dao=new CatalogDao();
  @Override protected void doGet(HttpServletRequest req,HttpServletResponse res) throws ServletException,IOException {
    res.setHeader("Cache-Control","no-store");
    try(Connection conn=DbPool.getConnection()) {
      var latest=dao.search(conn,CatalogQuery.parse(null,null,null,"relevance","1"));
      req.setAttribute("featured",latest.getCards().stream().limit(4).toList());
      req.getRequestDispatcher("/WEB-INF/views/featured-products.jsp").forward(req,res);
    } catch(SQLException|IllegalStateException ex){getServletContext().log("Homepage featured catalogue unavailable",ex);res.sendError(503,"Catalogue temporarily unavailable");}
  }
}

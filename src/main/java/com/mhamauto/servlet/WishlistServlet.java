package com.mhamauto.servlet;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/** Browser-local shortlist, not a server account wishlist. */
@WebServlet(name="WishlistServlet",urlPatterns="/wishlist")
public final class WishlistServlet extends HttpServlet {
  @Override protected void doGet(HttpServletRequest req,HttpServletResponse res) throws IOException,ServletException {
    res.setHeader("Cache-Control","no-store");
    req.getRequestDispatcher("/WEB-INF/views/wishlist.jsp").forward(req,res);
  }
}

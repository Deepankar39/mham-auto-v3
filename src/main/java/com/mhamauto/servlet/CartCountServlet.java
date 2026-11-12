package com.mhamauto.servlet;

import com.mhamauto.auth.AuthPrincipal;
import com.mhamauto.auth.AuthService;
import com.mhamauto.auth.SessionAuth;
import com.mhamauto.cart.CartService;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;

/** Tiny same-origin GET used for dynamic navigation count; does not create a cart. */
@WebServlet(name="CartCountServlet",urlPatterns="/api/cart/count")
public final class CartCountServlet extends HttpServlet {
    private final CartService carts=new CartService();
    @Override protected void doGet(HttpServletRequest req,HttpServletResponse res) throws IOException {
        res.setContentType("application/json;charset=UTF-8");
        res.setHeader("Cache-Control","private, no-store");
        res.setHeader("X-Content-Type-Options","nosniff");
        AuthPrincipal user=SessionAuth.principal(req);
        if(user==null) {res.getWriter().write("{\"count\":0}");return;}
        try {
            AuthPrincipal fresh=AuthService.refresh(user.id());
            if(fresh==null) {
                SessionAuth.signOut(req);
                res.getWriter().write("{\"count\":0}");return;
            }
            req.getSession(false).setAttribute(SessionAuth.USER_KEY,fresh);
            int n=carts.count(fresh.id());
            res.getWriter().write("{\"count\":"+n+"}");
        } catch(SQLException | IllegalStateException failure) {
            getServletContext().log("Cart badge unavailable",failure);
            res.setStatus(503);
            res.getWriter().write("{\"error\":\"temporarily unavailable\"}");
        }
    }
}

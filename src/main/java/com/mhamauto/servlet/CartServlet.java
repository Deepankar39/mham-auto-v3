package com.mhamauto.servlet;

import com.mhamauto.auth.AuthPrincipal;
import com.mhamauto.auth.Csrf;
import com.mhamauto.auth.SessionAuth;
import com.mhamauto.cart.CartConflict;
import com.mhamauto.cart.CartRules;
import com.mhamauto.cart.CartService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;

/** Authenticated, CSRF-protected POST-only mutation; GET for rendering only. */
@WebServlet(name="CartServlet",urlPatterns="/cart")
public final class CartServlet extends HttpServlet {
    private final CartService carts=new CartService();
    @Override protected void doGet(HttpServletRequest req,HttpServletResponse res) throws ServletException,IOException {
        noStore(res);
        AuthPrincipal user=SessionAuth.principal(req);
        if(user==null) {res.sendRedirect(req.getContextPath()+"/login");return;}
        try {
            req.setAttribute("cart",carts.read(user.id()));
            req.setAttribute("csrfToken",Csrf.issue(req));
            req.setAttribute("notice",notice(req.getParameter("notice")));
            req.getRequestDispatcher("/WEB-INF/views/cart.jsp").forward(req,res);
        } catch(SQLException | IllegalStateException e) {
            getServletContext().log("Cart read failure",e);
            res.sendError(503,"Cart temporarily unavailable");
        }
    }
    @Override protected void doPost(HttpServletRequest req,HttpServletResponse res) throws ServletException,IOException {
        noStore(res);
        AuthPrincipal user=SessionAuth.principal(req);
        if(user==null) {res.sendRedirect(req.getContextPath()+"/login");return;}
        if(!Csrf.valid(req)) {res.sendError(403,"Invalid or expired security token. Reload and retry.");return;}
        try {
            String action=req.getParameter("action");
            if(action==null) throw new IllegalArgumentException("Missing action");
            if(!action.equals("add") && !action.equals("update") && !action.equals("remove") && !action.equals("clear"))
                throw new IllegalArgumentException("Unknown cart action");
            long variantId="clear".equals(action)?0:CartRules.variantId(req.getParameter("variantId"));
            int quantity=action.equals("add") || action.equals("update")?CartRules.quantity(req.getParameter("quantity")):0;
            carts.change(user.id(),action,variantId,quantity);
            res.setStatus(HttpServletResponse.SC_SEE_OTHER);
            res.setHeader("Location",res.encodeRedirectURL(req.getContextPath()+"/cart?notice="+action));
        } catch(CartConflict e) {
            res.sendError(409,e.getMessage());
        } catch(IllegalArgumentException e) {
            res.sendError(400,e.getMessage());
        } catch(SQLException | IllegalStateException e) {
            getServletContext().log("Cart update failure",e);
            res.sendError(503,"Cart temporarily unavailable");
        }
    }
    private static void noStore(HttpServletResponse r) {r.setHeader("Cache-Control","no-store, no-cache, must-revalidate");}
    private static String notice(String code) {
        if(code==null) return "";
        return switch(code) {
            case "add" -> "Item added to your cart.";
            case "update" -> "Quantity updated.";
            case "remove" -> "Item removed.";
            case "clear" -> "Your cart has been cleared.";
            default -> "";
        };
    }
}

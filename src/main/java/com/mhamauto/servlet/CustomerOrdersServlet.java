package com.mhamauto.servlet;

import com.mhamauto.account.PortalDao;
import com.mhamauto.account.PortalRules;
import com.mhamauto.auth.AuthPrincipal;
import com.mhamauto.auth.SessionAuth;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.sql.SQLException;

/** Read-only, customer-owned demonstration orders. Never accepts a user ID parameter. */
@WebServlet(name="CustomerOrdersServlet",urlPatterns={"/orders","/orders/view"})
public final class CustomerOrdersServlet extends HttpServlet {
    private final PortalDao portal=new PortalDao();
    @Override protected void doGet(HttpServletRequest req,HttpServletResponse res) throws IOException,ServletException {
        res.setHeader("Cache-Control","no-store, no-cache, must-revalidate");
        AuthPrincipal user=SessionAuth.principal(req);
        if(user==null){res.sendRedirect(req.getContextPath()+"/login");return;}
        try {
            if("/orders/view".equals(req.getServletPath())) {
                String reference=PortalRules.reference(req.getParameter("ref"));
                var order=portal.order(user.id(),reference);
                // 404 for any other customer's order; never reveal whether a reference exists.
                if(order==null){res.sendError(404,"Demo order not found");return;}
                req.setAttribute("order",order);
                req.getRequestDispatcher("/WEB-INF/views/order-detail.jsp").forward(req,res);
            }else{
                var page=portal.orders(user.id(),PortalRules.page(req.getParameter("page")));
                req.setAttribute("ordersPage",page);
                req.getRequestDispatcher("/WEB-INF/views/orders.jsp").forward(req,res);
            }
        }catch(IllegalArgumentException e){res.sendError(400,"Invalid order history request");}
        catch(SQLException|IllegalStateException e){getServletContext().log("Order history read failed",e);res.sendError(503,"Order history temporarily unavailable");}
    }
    @Override protected void doPost(HttpServletRequest req,HttpServletResponse res) throws IOException {res.sendError(405,"Order history is read only");}
}

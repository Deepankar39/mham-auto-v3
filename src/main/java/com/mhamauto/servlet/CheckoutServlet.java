package com.mhamauto.servlet;

import com.mhamauto.auth.*;
import com.mhamauto.checkout.*;
import com.mhamauto.cart.CartModels;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.sql.SQLException;

@WebServlet(name="CheckoutServlet",urlPatterns={"/checkout","/order-confirmation"})
public final class CheckoutServlet extends HttpServlet {
    private final CheckoutService service=new CheckoutService();
    @Override protected void doGet(HttpServletRequest req,HttpServletResponse res) throws ServletException,IOException {
        noStore(res);AuthPrincipal user=SessionAuth.principal(req);
        if(user==null){res.sendRedirect(req.getContextPath()+"/login");return;}
        try{
            if("/order-confirmation".equals(req.getServletPath())){
                String ref=req.getParameter("ref");
                if(ref==null || !ref.matches("DEMO-[A-F0-9]{20}")){res.sendError(400,"Invalid order reference");return;}
                var order=service.order(user.id(),ref);
                if(order==null){res.sendError(404,"Order not found");return;}
                req.setAttribute("order",order);
                req.getRequestDispatcher("/WEB-INF/views/order-confirmation.jsp").forward(req,res);
                return;
            }
            CartModels.Summary summary=service.preview(user.id());
            req.setAttribute("cart",summary);
            req.setAttribute("addressList",service.addresses(user.id()));
            req.setAttribute("canCheckout",summary.getHasItems() && summary.getHasCompleteSubtotal());
            req.setAttribute("csrfToken",Csrf.issue(req));
            req.setAttribute("checkoutToken",CheckoutToken.issue(req));
            req.getRequestDispatcher("/WEB-INF/views/checkout.jsp").forward(req,res);
        }catch(SQLException|IllegalStateException e){getServletContext().log("Checkout display failure",e);res.sendError(503,"Checkout temporarily unavailable");}
    }
    @Override protected void doPost(HttpServletRequest req,HttpServletResponse res) throws IOException {
        noStore(res);AuthPrincipal user=SessionAuth.principal(req);
        if(user==null){res.sendRedirect(req.getContextPath()+"/login");return;}
        if(!"/checkout".equals(req.getServletPath())){res.sendError(405);return;}
        if(!Csrf.valid(req)){res.sendError(403,"Invalid security token");return;}
        try{
            String token=CheckoutRules.token(req.getParameter("checkoutToken"));
            long addressId=CheckoutRules.positiveId(req.getParameter("addressId"));
            String ref=service.checkout(user.id(),addressId,token,CheckoutToken.matches(req,token));
            // If a repeated POST returns an existing order, this still does not create a second order.
            CheckoutToken.rotate(req);
            res.setStatus(303);
            res.setHeader("Location",res.encodeRedirectURL(req.getContextPath()+"/order-confirmation?ref="+ref));
        }catch(CheckoutConflict e){res.sendError(409,e.getMessage());}
        catch(IllegalArgumentException e){res.sendError(400,e.getMessage());}
        catch(SQLException|IllegalStateException e){getServletContext().log("Demo order transaction failed",e);res.sendError(503,"Checkout temporarily unavailable");}
    }
    private static void noStore(HttpServletResponse res){res.setHeader("Cache-Control","no-store, no-cache, must-revalidate");}
}

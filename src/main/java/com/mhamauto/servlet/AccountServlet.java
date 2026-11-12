package com.mhamauto.servlet;

import com.mhamauto.account.PortalDao;
import com.mhamauto.auth.*;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.sql.SQLException;

@WebServlet(name="AccountServlet",urlPatterns={"/account"})
public final class AccountServlet extends HttpServlet {
    private final PortalDao portal=new PortalDao();
    @Override protected void doGet(HttpServletRequest req,HttpServletResponse res) throws IOException,ServletException {
        res.setHeader("Cache-Control","no-store, no-cache, must-revalidate");
        AuthPrincipal user=SessionAuth.principal(req);
        if(user==null){res.sendRedirect(req.getContextPath()+"/login");return;}
        try {
            req.setAttribute("csrfToken",Csrf.issue(req));
            req.setAttribute("dashboard",portal.dashboard(user.id()));req.setAttribute("profileUpdated","1".equals(req.getParameter("updated")));
            req.getRequestDispatcher("/WEB-INF/views/account.jsp").forward(req,res);
        }catch(SQLException|IllegalStateException e){getServletContext().log("Account dashboard failed",e);res.sendError(503,"Account dashboard temporarily unavailable");}
    }
    @Override protected void doPost(HttpServletRequest req,HttpServletResponse res) throws IOException,ServletException {
        res.setHeader("Cache-Control","no-store, no-cache, must-revalidate");
        if(!"/account".equals(req.getServletPath())){res.sendError(405);return;}
        AuthPrincipal user=SessionAuth.principal(req);
        if(user==null){res.sendRedirect(req.getContextPath()+"/login");return;}
        if(!Csrf.valid(req)){res.sendError(403,"Invalid security token");return;}
        if(!"updateName".equals(req.getParameter("action"))){res.sendError(400,"Invalid account action");return;}
        try {
            AuthPrincipal updated=portal.updateDisplayName(user.id(),req.getParameter("displayName"));
            HttpSession session=req.getSession(false);
            if(session==null){res.sendError(403,"Session expired");return;}
            session.setAttribute(SessionAuth.USER_KEY,updated);
            res.setStatus(303);
            res.setHeader("Location",res.encodeRedirectURL(req.getContextPath()+"/account?updated=1"));
        }catch(IllegalArgumentException validation){
            req.setAttribute("profileError",validation.getMessage());
            doGet(req,res);
        }catch(SQLException|IllegalStateException e){getServletContext().log("Account profile update failed",e);res.sendError(503,"Account update temporarily unavailable");}
    }
}

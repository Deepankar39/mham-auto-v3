package com.mhamauto.servlet;

import com.mhamauto.auth.*;
import com.mhamauto.trade.*;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.sql.SQLException;

/** Separate ADMIN-only controller; all protected routes also pass ProtectedPagesFilter. */
@WebServlet(name="AdminTradeServlet",urlPatterns={"/admin/trade-applications","/admin/trade-application"})
public final class AdminTradeServlet extends HttpServlet {
    private final TradeDao dao=new TradeDao();
    @Override protected void doGet(HttpServletRequest req,HttpServletResponse res)throws IOException,ServletException {
        noStore(res);
        AuthPrincipal actor=SessionAuth.principal(req);
        if(actor==null){res.sendRedirect(req.getContextPath()+"/login");return;}
        if(!actor.isAdmin()){res.sendError(403,"Administrator required");return;}
        req.setAttribute("adminPage",req.getServletPath());
        try {
            if("/admin/trade-applications".equals(req.getServletPath())) {
                String filter=TradeRules.listState(req.getParameter("status"));
                req.setAttribute("statusFilter",filter);
                req.setAttribute("summary",dao.counts());
                req.setAttribute("results",dao.list(TradeRules.page(req.getParameter("page")),filter));
                render(req,res,"admin-trade-applications.jsp");return;
            }
            if("/admin/trade-application".equals(req.getServletPath())) {
                var app=dao.detail(TradeRules.id(req.getParameter("id")));
                if(app==null){res.sendError(404);return;}
                req.setAttribute("application",app);
                req.setAttribute("saved","1".equals(req.getParameter("saved")));
                req.setAttribute("csrfToken",Csrf.issue(req));
                render(req,res,"admin-trade-application.jsp");return;
            }
            res.sendError(404);
        }catch(IllegalArgumentException ex){res.sendError(400,"Invalid trade admin request");}
        catch(SQLException|IllegalStateException ex){getServletContext().log("Trade admin read failed",ex);res.sendError(503,"Trade service temporarily unavailable");}
    }
    @Override protected void doPost(HttpServletRequest req,HttpServletResponse res)throws IOException,ServletException {
        noStore(res);
        AuthPrincipal actor=SessionAuth.principal(req);
        if(actor==null){res.sendError(401);return;}
        if(!actor.isAdmin()){res.sendError(403,"Administrator required");return;}
        if(!"/admin/trade-application".equals(req.getServletPath())){res.sendError(405);return;}
        if(!Csrf.valid(req)){res.sendError(403,"Invalid CSRF token");return;}
        try {
            long id=TradeRules.id(req.getParameter("id"));
            String decision=TradeRules.reviewState(req.getParameter("decision"));
            String note=TradeRules.field(req.getParameter("adminNote"),"Internal review note",0,500,true);
            if(!dao.review(id,actor.id(),decision,note)){res.sendError(404);return;}
            res.setStatus(303);res.setHeader("Location",res.encodeRedirectURL(req.getContextPath()+"/admin/trade-application?id="+id+"&saved=1"));
        }catch(IllegalArgumentException ex){res.sendError(400,ex.getMessage());}
        catch(IllegalStateException ex){res.sendError(409,"Application has already been reviewed");}
        catch(SQLException ex){getServletContext().log("Trade review failed",ex);res.sendError(503,"Trade review temporarily unavailable");}
    }
    private static void render(HttpServletRequest r,HttpServletResponse s,String file)throws ServletException,IOException {r.getRequestDispatcher("/WEB-INF/views/"+file).forward(r,s);}
    private static void noStore(HttpServletResponse s){s.setHeader("Cache-Control","no-store, no-cache, must-revalidate");s.setHeader("Pragma","no-cache");}
}

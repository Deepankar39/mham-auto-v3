package com.mhamauto.servlet;

import com.mhamauto.auth.*;
import com.mhamauto.trade.*;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.sql.SQLException;

/** Authenticated applicant routes; login and active-account checks are enforced by ProtectedPagesFilter. */
@WebServlet(name="TradeApplicationServlet",urlPatterns={"/trade/apply","/trade/status"})
public final class TradeApplicationServlet extends HttpServlet {
    private final TradeDao dao=new TradeDao();
    @Override protected void doGet(HttpServletRequest req,HttpServletResponse res)throws IOException,ServletException{
        noStore(res);
        AuthPrincipal actor=SessionAuth.principal(req);
        if(actor==null){res.sendRedirect(req.getContextPath()+"/login");return;}
        try {
            var existing=dao.mine(actor.id());
            if("/trade/status".equals(req.getServletPath())) {
                if(existing==null){redirect(req,res,"/trade/apply");return;}
                req.setAttribute("application",existing);
                render(req,res,"trade-status.jsp");return;
            }
            if(existing!=null){redirect(req,res,"/trade/status");return;}
            req.setAttribute("csrfToken",Csrf.issue(req));
            render(req,res,"trade-apply.jsp");
        }catch(SQLException|IllegalStateException e){getServletContext().log("Trade application read failed",e);res.sendError(503,"Trade service temporarily unavailable");}
    }
    @Override protected void doPost(HttpServletRequest req,HttpServletResponse res)throws IOException,ServletException{
        noStore(res);
        if(!"/trade/apply".equals(req.getServletPath())){res.sendError(405);return;}
        AuthPrincipal actor=SessionAuth.principal(req);
        if(actor==null){res.sendError(401);return;}
        if(!Csrf.valid(req)){res.sendError(403,"Invalid CSRF token");return;}
        try {
            var input=TradeRules.application(req.getParameter("companyName"),req.getParameter("contactName"),
                req.getParameter("phone"),req.getParameter("businessType"),req.getParameter("governorate"),
                req.getParameter("area"),req.getParameter("message"));
            dao.submit(actor.id(),input);
            redirect(req,res,"/trade/status");
        }catch(IllegalArgumentException e){
            req.setAttribute("errorMessage",e.getMessage());req.setAttribute("csrfToken",Csrf.issue(req));
            req.setAttribute("formCompany",req.getParameter("companyName"));
            req.setAttribute("formContact",req.getParameter("contactName"));
            req.setAttribute("formPhone",req.getParameter("phone"));
            req.setAttribute("formArea",req.getParameter("area"));
            req.setAttribute("formMessage",req.getParameter("message"));
            req.setAttribute("formType",req.getParameter("businessType"));
            req.setAttribute("formGov",req.getParameter("governorate"));
            res.setStatus(400);render(req,res,"trade-apply.jsp");
        }catch(SQLException e){
            if(e.getErrorCode()==1062)res.sendError(409,"An application already exists for this account");
            else {getServletContext().log("Trade application submission failed",e);res.sendError(503,"Trade service temporarily unavailable");}
        }catch(IllegalStateException e){getServletContext().log("Trade submission failed",e);res.sendError(503,"Trade service temporarily unavailable");}
    }
    private static void render(HttpServletRequest r,HttpServletResponse s,String file)throws IOException,ServletException{r.getRequestDispatcher("/WEB-INF/views/"+file).forward(r,s);}
    private static void redirect(HttpServletRequest r,HttpServletResponse s,String path){s.setStatus(303);s.setHeader("Location",s.encodeRedirectURL(r.getContextPath()+path));}
    private static void noStore(HttpServletResponse s){s.setHeader("Cache-Control","no-store, no-cache, must-revalidate");s.setHeader("Pragma","no-cache");}
}

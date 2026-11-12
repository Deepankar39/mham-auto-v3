package com.mhamauto.servlet;

import com.mhamauto.auth.AuthPrincipal;
import com.mhamauto.auth.AuthService;
import com.mhamauto.auth.Csrf;
import com.mhamauto.auth.LoginThrottle;
import com.mhamauto.auth.SessionAuth;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;

@WebServlet(name="IdentityServlet",urlPatterns={"/login","/register","/logout"})
public final class IdentityServlet extends HttpServlet {
    private final LoginThrottle throttle=new LoginThrottle();
    @Override protected void doGet(HttpServletRequest req,HttpServletResponse res) throws IOException,ServletException {
        noStore(res);
        String path=req.getServletPath();
        if("/logout".equals(path)) {res.sendError(405,"Logout requires POST");return;}
        if(SessionAuth.principal(req)!=null) {redirect(req,res,"/account");return;}
        render(req,res,"/register".equals(path));
    }
    @Override protected void doPost(HttpServletRequest req,HttpServletResponse res) throws IOException,ServletException {
        noStore(res);
        if(!Csrf.valid(req)) {res.sendError(403,"Security token expired. Reload the form and try again.");return;}
        String path=req.getServletPath();
        if("/logout".equals(path)) {SessionAuth.signOut(req);redirect(req,res,"/login");return;}
        if(SessionAuth.principal(req)!=null) {redirect(req,res,"/account");return;}
        if(!"/register".equals(path) && !"/login".equals(path)) {res.sendError(404);return;}
        try {
            AuthPrincipal principal;
            if("/register".equals(path)) {
                principal=AuthService.register(req.getParameter("name"),req.getParameter("email"),
                        req.getParameter("password"),req.getParameter("confirm"));
            } else {
                String ip=req.getRemoteAddr()==null?"unknown":req.getRemoteAddr();
                // Email is normalized only for the throttle identifier; never trust it as an identity.
                String email=req.getParameter("email");
                if(email==null) email="";
                String key=ip+"|"+(email.length()>254?email.substring(0,254):email).trim().toLowerCase(java.util.Locale.ROOT);
                long now=System.currentTimeMillis();
                if(!throttle.allowed(key,now)) {
                    req.setAttribute("error","Too many attempts. Try again in about 15 minutes.");
                    render(req,res,false);return;
                }
                principal=AuthService.login(email,req.getParameter("password"));
                if(principal==null) {
                    throttle.failed(key,now);
                    req.setAttribute("error","Incorrect email or password, or account unavailable.");
                    render(req,res,false);return;
                }
                throttle.succeeded(key);
            }
            SessionAuth.signIn(req,principal);
            redirect(req,res,"/account");
        } catch (IllegalArgumentException validation) {
            req.setAttribute("error",validation.getMessage());
            render(req,res,"/register".equals(path));
        } catch(SQLException | IllegalStateException database) {
            getServletContext().log("Phase 4 identity operation failed",database);
            res.sendError(503,"Account service temporarily unavailable");
        }
    }
    private static void noStore(HttpServletResponse res) {
        res.setHeader("Cache-Control","no-store, no-cache, must-revalidate");
        res.setHeader("Pragma","no-cache");
    }
    private static void redirect(HttpServletRequest req,HttpServletResponse res,String path) throws IOException {
        res.setStatus(HttpServletResponse.SC_SEE_OTHER);
        res.setHeader("Location",res.encodeRedirectURL(req.getContextPath()+path));
    }
    private static void render(HttpServletRequest req,HttpServletResponse res,boolean register)
            throws IOException,ServletException {
        req.setAttribute("csrfToken",Csrf.issue(req));
        req.getRequestDispatcher(register?"/WEB-INF/views/register.jsp":"/WEB-INF/views/login.jsp").forward(req,res);
    }
}

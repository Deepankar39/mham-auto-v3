package com.mhamauto.filter;

import com.mhamauto.auth.AuthPrincipal;
import com.mhamauto.auth.AuthService;
import com.mhamauto.auth.SessionAuth;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.SQLException;

/** Authorize each protected request on the server, refreshing disabled accounts/role changes from DB. */
@WebFilter(urlPatterns={"/account","/admin","/admin/*","/cart","/cart/*","/addresses","/checkout","/order-confirmation","/orders","/orders/*","/trade/apply","/trade/status","/trade/*"})
public final class ProtectedPagesFilter implements Filter {
    @Override public void doFilter(ServletRequest request,ServletResponse response,FilterChain chain)
            throws IOException,ServletException {
        HttpServletRequest req=(HttpServletRequest)request;
        HttpServletResponse res=(HttpServletResponse)response;
        res.setHeader("Cache-Control","no-store, no-cache, must-revalidate");
        res.setHeader("Pragma","no-cache");
        AuthPrincipal sessionUser=SessionAuth.principal(req);
        if(sessionUser==null) {res.sendRedirect(res.encodeRedirectURL(req.getContextPath()+"/login"));return;}
        try {
            AuthPrincipal fresh=AuthService.refresh(sessionUser.id());
            if(fresh==null) {SessionAuth.signOut(req);res.sendRedirect(req.getContextPath()+"/login");return;}
            HttpSession session=req.getSession(false);
            if(session==null) {res.sendRedirect(req.getContextPath()+"/login");return;}
            session.setAttribute(SessionAuth.USER_KEY,fresh);
            if(req.getServletPath().startsWith("/admin") && !fresh.isAdmin()) {
                res.sendError(403,"Administrator access required");return;
            }
            chain.doFilter(request,response);
        } catch (SQLException | IllegalStateException error) {
            req.getServletContext().log("Account authorization could not be verified",error);
            res.sendError(503,"Authentication service temporarily unavailable");
        }
    }
}

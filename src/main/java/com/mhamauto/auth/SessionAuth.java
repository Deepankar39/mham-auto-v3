package com.mhamauto.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

public final class SessionAuth {
    public static final String USER_KEY="authUser";
    private SessionAuth() { }
    public static AuthPrincipal principal(HttpServletRequest req) {
        HttpSession session=req.getSession(false);
        if (session==null) return null;
        Object principal=session.getAttribute(USER_KEY);
        return principal instanceof AuthPrincipal user ? user : null;
    }
    public static void signIn(HttpServletRequest req, AuthPrincipal principal) {
        HttpSession old=req.getSession(false);
        if (old != null) old.invalidate(); // prevents fixation; CSRF token rotates with new session
        HttpSession fresh=req.getSession(true);
        fresh.setMaxInactiveInterval(30*60);
        fresh.setAttribute(USER_KEY,principal);
        Csrf.issue(req);
    }
    public static void signOut(HttpServletRequest req) {
        HttpSession session=req.getSession(false);
        if (session != null) session.invalidate();
    }
}

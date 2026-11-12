package com.mhamauto.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

/** Synchronizer CSRF tokens for every state-changing identity request. */
public final class Csrf {
    private static final String KEY="mham_csrf";
    private static final SecureRandom RNG=new SecureRandom();
    private Csrf() { }
    public static String issue(HttpServletRequest request) {
        HttpSession session=request.getSession(true);
        synchronized (session) {
            Object existing=session.getAttribute(KEY);
            if (existing instanceof String token) return token;
            byte[] random=new byte[32]; RNG.nextBytes(random);
            String token=Base64.getUrlEncoder().withoutPadding().encodeToString(random);
            session.setAttribute(KEY,token);
            return token;
        }
    }
    public static boolean valid(HttpServletRequest req) {
        HttpSession session=req.getSession(false);
        if (session==null) return false;
        Object expected=session.getAttribute(KEY);
        String actual=req.getParameter("csrf");
        return expected instanceof String token && actual!=null && actual.length()<=80 &&
                MessageDigest.isEqual(token.getBytes(StandardCharsets.US_ASCII),actual.getBytes(StandardCharsets.US_ASCII));
    }
}

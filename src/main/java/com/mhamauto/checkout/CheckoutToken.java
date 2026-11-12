package com.mhamauto.checkout;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.security.SecureRandom;
import java.util.Base64;

/** One nonce per pending checkout; persisted unique constraint makes retry safe. */
public final class CheckoutToken {
    private static final SecureRandom RNG=new SecureRandom();
    private static final String KEY="mham_checkout_nonce";
    private CheckoutToken(){ }
    public static String issue(HttpServletRequest req){
        HttpSession s=req.getSession(true);
        synchronized(s){
            Object token=s.getAttribute(KEY);
            if(token instanceof String current) return current;
            byte[] bytes=new byte[32];RNG.nextBytes(bytes);
            String created=Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
            s.setAttribute(KEY,created);return created;
        }
    }
    public static boolean matches(HttpServletRequest req,String token){
        HttpSession s=req.getSession(false);
        return s!=null && token!=null && token.equals(s.getAttribute(KEY));
    }
    public static void rotate(HttpServletRequest req){
        HttpSession s=req.getSession(false);
        if(s!=null)s.removeAttribute(KEY);
    }
}

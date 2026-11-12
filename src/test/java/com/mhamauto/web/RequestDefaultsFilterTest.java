package com.mhamauto.web;

import com.mhamauto.filter.RequestDefaultsFilter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/** Direct filter behavior regression, without booting Tomcat. */
class RequestDefaultsFilterTest {
    record Check(Map<String,String> headers, int status, boolean forwarded) {}

    private static Object defaultValue(Class<?> type) {
        if (type == boolean.class) return false;
        if (type == int.class) return 0;
        if (type == long.class) return 0L;
        return null;
    }
    private Check filter(String method, String path, String origin) throws Exception {
        Map<String,String> headers = new HashMap<>();
        AtomicBoolean called = new AtomicBoolean();
        AtomicInteger status = new AtomicInteger(200);
        HttpServletRequest req=(HttpServletRequest)Proxy.newProxyInstance(getClass().getClassLoader(),
                new Class[]{HttpServletRequest.class}, (proxy,m,args)-> switch(m.getName()) {
                    case "getCharacterEncoding" -> "UTF-8";
                    case "getMethod" -> method;
                    case "getServletPath" -> path;
                    case "getHeader" -> "Origin".equals(args[0]) ? origin : null;
                    case "getScheme" -> "http";
                    case "getServerName" -> "localhost";
                    case "getServerPort" -> 8080;
                    default -> defaultValue(m.getReturnType());
                });
        HttpServletResponse res=(HttpServletResponse)Proxy.newProxyInstance(getClass().getClassLoader(),
                new Class[]{HttpServletResponse.class}, (proxy,m,args)-> {
                    if (m.getName().equals("setHeader")) headers.put((String)args[0],(String)args[1]);
                    if (m.getName().equals("sendError")) status.set((Integer)args[0]);
                    return defaultValue(m.getReturnType());
                });
        FilterChain chain=(request,response)->called.set(true);
        new RequestDefaultsFilter().doFilter(req,res,chain);
        return new Check(headers,status.get(),called.get());
    }
    @Test void cspAndHeaders() throws Exception {
        Check c=filter("GET","/catalog",null);
        assertEquals(200,c.status()); assertTrue(c.forwarded());
        assertTrue(c.headers().get("Content-Security-Policy").contains("script-src 'self'"));
        assertEquals("nosniff",c.headers().get("X-Content-Type-Options"));
        assertNull(c.headers().get("Cache-Control"));
    }
    @Test void cacheOnlyForPublicAssets() throws Exception {
        Check c=filter("GET","/assets/css/phase10.css",null);
        assertTrue(c.headers().get("Cache-Control").contains("max-age=300"));
    }
    @Test void blockHostileOriginAndTrace() throws Exception {
        Check denied=filter("POST","/login","http://evil.example");
        assertEquals(403,denied.status()); assertFalse(denied.forwarded());
        Check trace=filter("TRACE","/",null);
        assertEquals(405,trace.status()); assertFalse(trace.forwarded());
    }
    @Test void allowSameOriginButCsrfStillCheckedByServlet() throws Exception {
        Check accepted=filter("POST","/login","http://localhost:8080");
        assertEquals(200,accepted.status()); assertTrue(accepted.forwarded());
    }
}

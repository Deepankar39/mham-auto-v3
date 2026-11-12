package com.mhamauto.auth;

import java.util.concurrent.ConcurrentHashMap;

/** Small, process-local demo throttle. Not distributed or a substitute for edge anti-abuse controls. */
public final class LoginThrottle {
    private static final long WINDOW=15*60*1000L;
    private static final int LIMIT=6;
    private static final int MAX_KEYS=10_000;
    private final ConcurrentHashMap<String,Attempt> attempts=new ConcurrentHashMap<>();
    private record Attempt(long windowStart,int failures) {}
    public boolean allowed(String key,long now) {
        Attempt attempt=attempts.get(key);
        return attempt == null || now-attempt.windowStart()>=WINDOW || attempt.failures()<LIMIT;
    }
    public void failed(String key,long now) {
        if (attempts.size()>MAX_KEYS) attempts.entrySet().removeIf(e->now-e.getValue().windowStart()>=WINDOW);
        if (attempts.size()>MAX_KEYS) return; // bounded memory; edge rate limiting still required in production
        attempts.compute(key,(k,old)->old==null || now-old.windowStart()>=WINDOW
                ? new Attempt(now,1) : new Attempt(old.windowStart(),Math.min(LIMIT,old.failures()+1)));
    }
    public void succeeded(String key) {attempts.remove(key);}
}

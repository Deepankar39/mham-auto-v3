package com.mhamauto.auth;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class LoginThrottleTest {
    @Test void lockoutAndExpiry() {
        LoginThrottle throttle=new LoginThrottle();
        long now=12345678L;
        for(int i=0;i<6;i++){assertTrue(throttle.allowed("ip|email",now));throttle.failed("ip|email",now);}
        assertFalse(throttle.allowed("ip|email",now));
        assertTrue(throttle.allowed("ip|email",now+15*60*1000L));
        throttle.succeeded("ip|email");
        assertTrue(throttle.allowed("ip|email",now));
    }
}

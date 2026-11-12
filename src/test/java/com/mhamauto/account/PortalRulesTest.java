package com.mhamauto.account;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PortalRulesTest {
    @Test void pageValidation() {
        assertEquals(1,PortalRules.page(null));
        assertEquals(1,PortalRules.page(""));
        assertEquals(12,PortalRules.page("12"));
        assertEquals(48,PortalRules.offset(5));
        for(String bad:new String[]{"0","-1","1 OR 1=1","5001","1.0"," 3","9999999999999"}){
            assertThrows(IllegalArgumentException.class,()->PortalRules.page(bad));
        }
    }
    @Test void referenceAndDisplayName() {
        assertEquals("DEMO-0123456789ABCDEFABCD",PortalRules.reference("DEMO-0123456789ABCDEFABCD"));
        for(String invalid:new String[]{"DEMO-abc","anything","DEMO-0123456789ABCDEFABCD' OR 1=1"})
            assertThrows(IllegalArgumentException.class,()->PortalRules.reference(invalid));
        assertEquals("My Shop",PortalRules.displayName("  My Shop  "));
        assertThrows(IllegalArgumentException.class,()->PortalRules.displayName("A"));
    }
    @Test void labelsAreAllowlisted() {
        assertEquals("Demo placed",PortalRules.statusLabel("DEMO_PLACED"));
        assertEquals("Demo status unknown",PortalRules.statusLabel("CUSTOM_ADMIN"));
    }
}

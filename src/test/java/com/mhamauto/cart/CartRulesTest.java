package com.mhamauto.cart;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;
class CartRulesTest {
    @Test void quantity() {
        assertEquals(20,CartRules.quantity("20"));
        for(String raw:new String[]{"0","21","-1","2.0","1 OR 1=1",""})
            assertThrows(IllegalArgumentException.class,()->CartRules.quantity(raw));
    }
    @Test void precision() {
        assertEquals("KWD 3.705",CartRules.money(CartRules.lineTotal(new BigDecimal("1.235"),3)));
        assertEquals(null,CartRules.lineTotal(null,3));
    }
    @Test void stock() {
        assertFalse(CartRules.quantityWithinSample(3,2));
        assertTrue(CartRules.quantityWithinSample(3,null));
    }
}

package com.mhamauto.checkout;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;

class CheckoutRulesTest {
    @Test void keepsFilsExact(){assertEquals(new BigDecimal("30.375"),CheckoutRules.line(new BigDecimal("10.125"),3));}
    @Test void neverAcceptsUnverifiedPrices(){assertThrows(IllegalArgumentException.class,()->CheckoutRules.line(null,1));}
    @Test void rejectsTooMuchPrecision(){assertThrows(IllegalArgumentException.class,()->CheckoutRules.price(new BigDecimal("1.0001")));}
    @Test void rejectsOverflowQty(){assertThrows(IllegalArgumentException.class,()->CheckoutRules.line(BigDecimal.ONE,21));}
    @Test void validatesIdsAndTokens(){assertThrows(IllegalArgumentException.class,()->CheckoutRules.positiveId("-1"));assertThrows(IllegalArgumentException.class,()->CheckoutRules.token("bad"));}
}

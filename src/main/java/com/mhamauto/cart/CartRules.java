package com.mhamauto.cart;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** No client-provided prices or totals are accepted. All money is KWD decimal(18,3). */
public final class CartRules {
    public static final int MAX_QUANTITY=20;
    private CartRules() { }
    public static int quantity(String raw) {
        if(raw==null || !raw.matches("[1-9][0-9]?")) throw new IllegalArgumentException("Quantity must be between 1 and 20");
        int n=Integer.parseInt(raw);
        if(n>MAX_QUANTITY) throw new IllegalArgumentException("Quantity must be between 1 and 20");
        return n;
    }
    public static long variantId(String raw) {
        if(raw==null || !raw.matches("[1-9][0-9]{0,18}")) throw new IllegalArgumentException("Invalid variant identifier");
        try {return Long.parseLong(raw);} catch(NumberFormatException ex) {throw new IllegalArgumentException("Invalid variant identifier");}
    }
    public static BigDecimal lineTotal(BigDecimal unitPrice, int qty) {
        if(unitPrice==null) return null;
        if(qty<1 || qty>MAX_QUANTITY || unitPrice.signum()<0 || unitPrice.scale()>3)
            throw new IllegalArgumentException("Invalid price or quantity");
        return unitPrice.multiply(BigDecimal.valueOf(qty)).setScale(3,RoundingMode.UNNECESSARY);
    }
    public static String money(BigDecimal amount) {
        return amount==null?"Price pending verification":"KWD "+amount.setScale(3,RoundingMode.UNNECESSARY).toPlainString();
    }
    public static boolean quantityWithinSample(int requested,Integer sourceQuantity) {
        return requested>=1 && requested<=MAX_QUANTITY && (sourceQuantity==null || requested<=sourceQuantity);
    }
}

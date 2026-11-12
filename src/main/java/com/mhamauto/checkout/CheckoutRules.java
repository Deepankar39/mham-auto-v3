package com.mhamauto.checkout;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** Pure rules: exact KWD amounts; no fees, payments or inventory reservation. */
public final class CheckoutRules {
    private CheckoutRules() { }
    public static BigDecimal price(BigDecimal value) {
        if(value==null) throw new IllegalArgumentException("KWD pricing has not been verified for every cart item");
        if(value.signum()<0 || value.scale()>3 || value.precision()>18)
            throw new IllegalArgumentException("Invalid three-decimal KWD price");
        return value.setScale(3,RoundingMode.UNNECESSARY);
    }
    public static BigDecimal line(BigDecimal unit,int quantity) {
        if(quantity<1 || quantity>20) throw new IllegalArgumentException("Quantity must be 1–20");
        BigDecimal amount=price(unit).multiply(BigDecimal.valueOf(quantity));
        if(amount.precision()>18) throw new IllegalArgumentException("Order amount exceeds the allowed range");
        return amount;
    }
    public static String money(BigDecimal amount) {return "KWD "+price(amount).toPlainString();}
    public static long positiveId(String raw) {
        if(raw==null || !raw.matches("[1-9][0-9]{0,18}")) throw new IllegalArgumentException("Invalid identifier");
        try {return Long.parseLong(raw);}catch(NumberFormatException e){throw new IllegalArgumentException("Invalid identifier");}
    }
    public static String token(String raw) {
        if(raw==null || !raw.matches("[A-Za-z0-9_-]{43}")) throw new IllegalArgumentException("Invalid checkout request token");
        return raw;
    }
}

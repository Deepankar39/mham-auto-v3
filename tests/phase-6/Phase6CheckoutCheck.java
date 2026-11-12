package com.mhamauto.checkout;

import java.math.BigDecimal;

/** Runnable without Maven/MySQL/Tomcat. */
public final class Phase6CheckoutCheck {
    public static void main(String[] args) {
        eq(AddressRules.phone("+965 51234567"),"+96551234567");
        eq(AddressRules.phone("51234567"),"+96551234567");
        eq(AddressRules.phone("00965 61234567"),"+96561234567");
        expectBad(()->AddressRules.phone("+44 12345678"));
        expectBad(()->AddressRules.phone("12345"));
        var a=AddressRules.parse("Kuwait Test","51234567","HAWALLI","Salmiya","4","Al Salem Street","22","3","Near landmark");
        eq(a.governorate(),"HAWALLI");
        expectBad(()->AddressRules.parse("Test","51234567","INVALID","Salmiya","4","Street","22","",""));
        expectBad(()->AddressRules.parse("<script>","51234567","HAWALLI","Salmiya","4","Street","22","",""));
        eq(CheckoutRules.line(new BigDecimal("10.125"),3),new BigDecimal("30.375"));
        eq(CheckoutRules.money(new BigDecimal("0")),"KWD 0.000");
        expectBad(()->CheckoutRules.line(null,1));
        expectBad(()->CheckoutRules.line(new BigDecimal("1.0012"),1));
        expectBad(()->CheckoutRules.line(new BigDecimal("1.000"),21));
        expectBad(()->CheckoutRules.price(new BigDecimal("-1")));
        expectBad(()->CheckoutRules.token("not-a-token"));
        eq(CheckoutRules.positiveId("142"),142L);
        expectBad(()->CheckoutRules.positiveId("0 OR 1=1"));
        System.out.println("PASS Phase 6 offline: governorates, phone, field safety, precise totals, quantity, tokens");
    }
    private static void eq(Object a,Object b){if(!a.equals(b))throw new AssertionError(a+" != "+b);}
    private static void expectBad(Runnable r){try{r.run();throw new AssertionError("Expected validation failure");}catch(IllegalArgumentException expected){/*ok*/}}
}

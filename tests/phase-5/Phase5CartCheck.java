package com.mhamauto.cart;

import java.math.BigDecimal;
import java.util.List;
import com.mhamauto.cart.CartModels.*;

/** Standalone offline source checks; requires only javac/java 21 and prior Phase 1.4 Image URL policy. */
public final class Phase5CartCheck {
    private static void check(boolean b,String label) {if(!b) throw new AssertionError(label);}
    private static void reject(Runnable r) {try {r.run();throw new AssertionError("expected rejection");}catch(IllegalArgumentException expected){}}
    public static void main(String[] args) {
        check(CartRules.quantity("1")==1 && CartRules.quantity("20")==20,"valid quantity");
        for(String s:new String[]{"0","21","999","-1","1.0","1 OR 1=1","","01"," 2 ","999999999999999999999"})
            reject(()->CartRules.quantity(s));
        check(CartRules.variantId("123") == 123L,"valid variant");
        for(String s:new String[]{"0","-1","1 OR 1=1","9999999999999999999999999999",""}) reject(()->CartRules.variantId(s));
        check(CartRules.money(new BigDecimal("1.250")).equals("KWD 1.250"),"kwd precision");
        check(CartRules.lineTotal(new BigDecimal("1.235"),3).equals(new BigDecimal("3.705")),"server monetary arithmetic");
        check(CartRules.lineTotal(null,2)==null,"unverified price never fabricated");
        reject(()->CartRules.lineTotal(new BigDecimal("-1"),1));
        reject(()->CartRules.lineTotal(new BigDecimal("1.2345"),1));
        check(CartRules.quantityWithinSample(2,2) && !CartRules.quantityWithinSample(3,2),"source quantity limit");
        check(CartRules.quantityWithinSample(5,null),"unknown source quantity");
        Item pending = new Item(3,"good-handle","Demo Part","SKU", "", "https://evil.example/test.jpg",
                2,null,500,false,true);
        check(pending.getImageUrl().isBlank(),"media allowlist");
        check(pending.getLineTotalLabel().equals("Price pending verification"),"uncertain price displayed safely");
        Summary summary = new Summary(List.of(pending),1,2,new BigDecimal("0.000"),true,false);
        check(!summary.getHasCompleteSubtotal() && summary.getSubtotalLabel().equals("Pending verification"),"subtotal uncertainty");
        Item hidden = new Item(4,"old","Old Part","SKU2","","",1,new BigDecimal("2.000"),5,false,false);
        check(!hidden.getAvailable(),"inactive item blocked");
        System.out.println("PASS Phase 5 offline checks: bounds, identifiers, precision, media safety, pending-price and unavailable handling");
    }
}

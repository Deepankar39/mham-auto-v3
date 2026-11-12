package com.mhamauto.account;
/** Plain-Java check; no external libraries or server required. */
public final class Phase7PortalCheck {
    private static void check(boolean ok,String label){if(!ok)throw new AssertionError(label);}
    public static void main(String[] args){
        check(PortalRules.page(null)==1,"default page");
        check(PortalRules.page("9")==9,"page parse");
        check(PortalRules.offset(9)==96,"page offset");
        try{PortalRules.page("1 OR 1=1");throw new AssertionError("Injected page was accepted");}
        catch(IllegalArgumentException expected){}
        try{PortalRules.page("5001");throw new AssertionError("Too deep page");}
        catch(IllegalArgumentException expected){}
        check("DEMO-0123456789ABCDEFABCD".equals(PortalRules.reference("DEMO-0123456789ABCDEFABCD")),"reference");
        try{PortalRules.reference("DEMO-BAD");throw new AssertionError("Bad reference");}
        catch(IllegalArgumentException expected){}
        check("Customer Name".equals(PortalRules.displayName(" Customer Name ")),"profile normalization");
        try{PortalRules.displayName("A");throw new AssertionError("Bad name");}
        catch(IllegalArgumentException expected){}
        check("Demo reviewed".equals(PortalRules.statusLabel("DEMO_REVIEWED")),"status");
        System.out.println("PASS Phase 7 portal rules");
    }
}

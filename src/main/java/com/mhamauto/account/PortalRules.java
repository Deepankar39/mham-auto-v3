package com.mhamauto.account;

import com.mhamauto.auth.IdentityRules;
import java.util.regex.Pattern;

/** Strict request validation shared by account and order-history endpoints. */
public final class PortalRules {
    private static final Pattern PAGE=Pattern.compile("[1-9][0-9]{0,3}");
    private static final Pattern REFERENCE=Pattern.compile("DEMO-[A-F0-9]{20}");
    public static final int PAGE_SIZE=12;
    private PortalRules() { }

    public static int page(String value) {
        if(value==null || value.isBlank())return 1;
        if(!PAGE.matcher(value).matches())throw new IllegalArgumentException("Invalid page number");
        int page=Integer.parseInt(value);
        if(page>5000)throw new IllegalArgumentException("Page limit exceeded");
        return page;
    }
    public static int offset(int page) {
        if(page<1 || page>5000)throw new IllegalArgumentException("Invalid page number");
        return Math.multiplyExact(page-1,PAGE_SIZE);
    }
    public static String reference(String value) {
        if(value==null || !REFERENCE.matcher(value).matches())throw new IllegalArgumentException("Invalid demo order reference");
        return value;
    }
    public static String displayName(String value) {return IdentityRules.displayName(value);}
    public static String statusLabel(String status) {
        return switch(status==null?"":status) {
            case "DEMO_PLACED" -> "Demo placed";
            case "DEMO_REVIEWED" -> "Demo reviewed";
            case "DEMO_CANCELLED" -> "Demo cancelled";
            default -> "Demo status unknown";
        };
    }
}

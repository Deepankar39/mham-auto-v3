package com.mhamauto.admin;

import java.math.BigDecimal;
import java.util.Locale;

/** All admin inputs are bounded. No arbitrary SQL identifiers or target status can enter DAO SQL. */
public final class AdminRules {
    private AdminRules() { }
    public static String required(String raw, String label, int max) {
        if(raw==null || raw.isBlank() || raw.length()>max) throw new IllegalArgumentException(label+" is required (max "+max+" characters)");
        String s=raw.strip();
        if(s.chars().anyMatch(c -> c<32 && c!='\n' && c!='\t' && c!='\r')) throw new IllegalArgumentException(label+" contains control characters");
        return s;
    }
    public static String optional(String raw,int max) {
        if(raw==null || raw.isBlank())return null;
        return required(raw,"Optional text",max);
    }
    public static long id(String raw) {
        if(raw==null || !raw.matches("[1-9][0-9]{0,18}"))throw new IllegalArgumentException("Invalid identifier");
        try{return Long.parseLong(raw);}catch(NumberFormatException e){throw new IllegalArgumentException("Invalid identifier");}
    }
    public static Long optionalId(String raw){return raw==null||raw.isBlank()?null:id(raw);}
    public static int page(String raw) {
        if(raw==null||raw.isBlank())return 1;
        if(!raw.matches("[1-9][0-9]{0,5}"))throw new IllegalArgumentException("Invalid page");
        int n=Integer.parseInt(raw);if(n>10000)throw new IllegalArgumentException("Page limit exceeded");return n;
    }
    public static String handle(String raw) {
        String s=required(raw,"Handle",191).toLowerCase(Locale.ROOT);
        if(!s.matches("[a-z0-9]+(?:-[a-z0-9]+)*"))throw new IllegalArgumentException("Handle must contain lowercase letters, numbers and hyphens only");
        return s;
    }
    public static String sku(String raw){return required(raw,"SKU",191);}
    public static String status(String raw){
        if(!"active".equals(raw)&&!"draft".equals(raw)&&!"archived".equals(raw))throw new IllegalArgumentException("Invalid product status");
        return raw;
    }
    public static boolean published(String raw){
        if("true".equals(raw))return true;
        if("false".equals(raw))return false;
        throw new IllegalArgumentException("Invalid publication flag");
    }
    public static Integer stock(String raw){
        if(raw==null||raw.isBlank())return null;
        if(!raw.matches("0|[1-9][0-9]{0,7}"))throw new IllegalArgumentException("Stock must be 0–99999999 or blank");
        return Integer.parseInt(raw);
    }
    public static BigDecimal demoPrice(String raw) {
        if(raw==null||raw.isBlank())return null;
        if(!raw.matches("(?:0|[1-9][0-9]{0,11})(?:\\.[0-9]{1,3})?"))throw new IllegalArgumentException("Demo KWD price must have up to 3 decimal places");
        return new BigDecimal(raw).setScale(3);
    }
    public static String nextOrderStatus(String oldStatus,String proposed) {
        if(!"DEMO_PLACED".equals(oldStatus) && !"DEMO_REVIEWED".equals(oldStatus) && !"DEMO_CANCELLED".equals(oldStatus))
            throw new IllegalArgumentException("Unknown existing status");
        if("DEMO_PLACED".equals(oldStatus) && ("DEMO_REVIEWED".equals(proposed)||"DEMO_CANCELLED".equals(proposed)))return proposed;
        if(oldStatus.equals(proposed))return proposed; // idempotent
        throw new IllegalArgumentException("Status change is not allowed for this demo order");
    }
    public static String escapedDescription(String text){
        String s=optional(text,10000);
        if(s==null)return null;
        return "<p>"+s.replace("&","&amp;").replace("<","&lt;").replace(">","&gt;").replace("\"","&quot;").replace("'","&#39;").replace("\r\n","\n").replace("\n","</p><p>")+"</p>";
    }
}

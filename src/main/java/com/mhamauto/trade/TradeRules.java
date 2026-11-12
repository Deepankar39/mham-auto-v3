package com.mhamauto.trade;

import com.mhamauto.checkout.AddressRules;
import java.util.List;

/** Pure server-side validation. Never interprets review status as permissions. */
public final class TradeRules {
    private TradeRules() { }
    public static final int PAGE_SIZE=15;
    public static final List<String> TYPES=List.of("GARAGE","WORKSHOP","PARTS_RETAILER","FLEET","OTHER");
    public static final List<String> REVIEW_STATES=List.of("PENDING","APPROVED","REJECTED");
    public static String field(String raw,String label,int min,int max,boolean optional) {
        String value=raw==null?"":raw.strip();
        if(optional && value.isEmpty())return "";
        if(value.length()<min||value.length()>max)throw new IllegalArgumentException(label+" must contain "+min+"–"+max+" characters");
        for(int i=0;i<value.length();i++) {
            char c=value.charAt(i);
            if(Character.isISOControl(c)||c=='<'||c=='>')throw new IllegalArgumentException(label+" contains unsupported characters");
        }
        return value;
    }
    public static Input application(String company,String contact,String phone,String type,String gov,String area,String note) {
        if(!TYPES.contains(type))throw new IllegalArgumentException("Select a valid business type");
        if(!AddressRules.GOVERNORATES.containsKey(gov))throw new IllegalArgumentException("Select a valid Kuwait governorate");
        return new Input(field(company,"Company",2,160,false),field(contact,"Contact name",2,100,false),
            AddressRules.phone(phone),type,gov,field(area,"Business area",2,90,false),field(note,"Message",0,500,true));
    }
    public static String reviewState(String raw) {
        if(!List.of("APPROVED","REJECTED").contains(raw))throw new IllegalArgumentException("Choose Approve or Reject");
        return raw;
    }
    public static String listState(String raw) {
        if(raw==null||raw.isBlank()||"ALL".equals(raw))return "ALL";
        if(!REVIEW_STATES.contains(raw))throw new IllegalArgumentException("Invalid status filter");
        return raw;
    }
    public static int page(String raw) {
        if(raw==null||raw.isBlank())return 1;
        if(!raw.matches("[1-9][0-9]{0,5}"))throw new IllegalArgumentException("Invalid page");
        int value=Integer.parseInt(raw);
        if(value>100000)throw new IllegalArgumentException("Page too large");
        return value;
    }
    public static long id(String raw) {
        if(raw==null||!raw.matches("[1-9][0-9]{0,18}"))throw new IllegalArgumentException("Invalid application ID");
        long value=Long.parseLong(raw);
        if(value<1)throw new IllegalArgumentException("Invalid application ID");
        return value;
    }
    public record Input(String companyName,String contactName,String phone,String businessType,
                        String governorate,String area,String applicantNote) { }
}

package com.mhamauto.checkout;

import java.util.List;
import java.util.Map;

/** Kuwait-only demonstration address validation, independent of Servlet/JDBC. */
public final class AddressRules {
    private AddressRules() { }
    public static final Map<String,String> GOVERNORATES=java.util.Collections.unmodifiableMap(new java.util.LinkedHashMap<>(Map.of(
        "CAPITAL","Al Asimah (Capital)", "HAWALLI","Hawalli", "FARWANIYA","Farwaniya",
        "AHMADI","Ahmadi", "JAHRA","Jahra", "MUBARAK_AL_KABEER","Mubarak Al-Kabeer")));
    public static List<String> codes() {return List.of("CAPITAL","HAWALLI","FARWANIYA","AHMADI","JAHRA","MUBARAK_AL_KABEER");}
    public static String governorateLabel(String code) {return GOVERNORATES.getOrDefault(code,"Unknown");}
    public static String phone(String raw) {
        if(raw==null) throw new IllegalArgumentException("Enter a Kuwait contact number");
        String value=raw.strip().replace(" ","").replace("-","");
        if(value.startsWith("00965")) value=value.substring(5);
        else if(value.startsWith("+965")) value=value.substring(4);
        else if(value.startsWith("965") && value.length()==11) value=value.substring(3);
        if(!value.matches("[2569][0-9]{7}")) throw new IllegalArgumentException("Use an 8-digit Kuwait phone number (optionally +965)");
        return "+965"+value;
    }
    public static String field(String raw,String name,int min,int max,boolean optional) {
        String value=raw==null?"":raw.strip();
        if(optional && value.isEmpty()) return "";
        if(value.length()<min || value.length()>max) throw new IllegalArgumentException(name+" must be between "+min+" and "+max+" characters");
        for(int i=0;i<value.length();i++) if(Character.isISOControl(value.charAt(i)) || value.charAt(i)=='<' || value.charAt(i)=='>')
            throw new IllegalArgumentException(name+" contains unsupported characters");
        return value;
    }
    public static AddressData parse(String name,String phone,String gov,String area,String block,String street,String building,String floor,String directions) {
        if(!GOVERNORATES.containsKey(gov)) throw new IllegalArgumentException("Select a valid Kuwait governorate");
        return new AddressData(field(name,"Recipient",2,100,false), phone(phone),gov,
            field(area,"Area",2,90,false),field(block,"Block",1,20,false),
            field(street,"Street",2,120,false),field(building,"Building",1,60,false),
            field(floor,"Floor / apartment",0,100,true),field(directions,"Directions",0,250,true));
    }
    public record AddressData(String recipientName,String phone,String governorate,String area,String block,
                              String street,String building,String floorDetail,String directions) { }
}

package com.mhamauto.checkout;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/** Snapshot records expose JavaBean getters for JSP EL. */
public final class CheckoutModels {
    private CheckoutModels() { }
    public record Address(long id,AddressRules.AddressData data) {
        public long getId(){return id;}
        public String getRecipientName(){return data.recipientName();}
        public String getPhone(){return data.phone();}
        public String getGovernorate(){return data.governorate();}
        public String getGovernorateLabel(){return AddressRules.governorateLabel(data.governorate());}
        public String getArea(){return data.area();}
        public String getBlock(){return data.block();}
        public String getStreet(){return data.street();}
        public String getBuilding(){return data.building();}
        public String getFloorDetail(){return data.floorDetail();}
        public String getDirections(){return data.directions();}
        public String getSummary(){return data.area()+", Block "+data.block()+", "+data.street()+", Building "+data.building();}
    }
    public record OrderLine(String sku,String title,int quantity,BigDecimal unitPrice,BigDecimal lineTotal,String priceBasis) {
        public boolean getFictionalPrice(){return "SYNTHETIC_DEMO".equals(priceBasis);}
        public String getSku(){return sku;}
        public String getTitle(){return title;}
        public int getQuantity(){return quantity;}
        public String getUnitPriceLabel(){return CheckoutRules.money(unitPrice);}
        public String getLineTotalLabel(){return CheckoutRules.money(lineTotal);}
    }
    public record Order(long id,String reference,String status,String createdAt,AddressRules.AddressData address,
                        BigDecimal subtotal,BigDecimal total,List<OrderLine> lines) {
        public long getId(){return id;}
        public String getReference(){return reference;}
        public String getStatus(){return status;}
        public String getCreatedAt(){return createdAt;}
        public String getRecipientName(){return address.recipientName();}
        public String getPhone(){return address.phone();}
        public String getGovernorateLabel(){return AddressRules.governorateLabel(address.governorate());}
        public String getAddressLabel(){return address.area()+", Block "+address.block()+", "+address.street()+", Building "+address.building();}
        public String getFloorDetail(){return address.floorDetail();}
        public String getDirections(){return address.directions();}
        public String getSubtotalLabel(){return CheckoutRules.money(subtotal);}
        public String getTotalLabel(){return CheckoutRules.money(total);}
        public List<OrderLine> getLines(){return lines;}
    }
}

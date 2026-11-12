package com.mhamauto.account;

import com.mhamauto.checkout.CheckoutRules;
import java.math.BigDecimal;
import java.util.List;

/** Explicit EL getters keep JSPs independent of record accessor conventions. */
public final class PortalModels {
    private PortalModels() { }
    public record OrderCard(String reference,String status,String createdAt,int lines,int units,BigDecimal total) {
        public String getReference(){return reference;}
        public String getStatus(){return status;}
        public String getStatusLabel(){return PortalRules.statusLabel(status);}
        public String getCreatedAt(){return createdAt;}
        public int getLines(){return lines;}
        public int getUnits(){return units;}
        public String getTotalLabel(){return CheckoutRules.money(total);}
    }
    public record Dashboard(long demoOrders,long savedAddresses,int cartUnits,BigDecimal demoTotal,List<OrderCard> recent) {
        public long getDemoOrders(){return demoOrders;}
        public long getSavedAddresses(){return savedAddresses;}
        public int getCartUnits(){return cartUnits;}
        public String getDemoTotalLabel(){return CheckoutRules.money(demoTotal);}
        public List<OrderCard> getRecent(){return recent;}
    }
    public record OrderPage(List<OrderCard> orders,int number,int pages,long total) {
        public List<OrderCard> getOrders(){return orders;}
        public int getNumber(){return number;}
        public int getPages(){return pages;}
        public long getTotal(){return total;}
        public boolean getHasPrevious(){return number>1;}
        public boolean getHasNext(){return number<pages;}
        public int getPrevious(){return number-1;}
        public int getNext(){return number+1;}
    }
}

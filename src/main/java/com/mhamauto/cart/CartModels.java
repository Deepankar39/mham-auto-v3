package com.mhamauto.cart;

import com.mhamauto.media.ExternalImageUrlPolicy;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

public final class CartModels {
    private CartModels() { }
    public record Item(long variantId,String handle,String title,String sku,String optionSummary,
                       String imageUrl,int quantity,BigDecimal unitPrice,Integer sourceQuantity,
                       boolean stockVerified,boolean visible) {
        public long getVariantId(){return variantId;}
        public String getTitle(){return title;}
        public String getSku(){return sku;}
        public String getOptionSummary(){return optionSummary;}
        public int getQuantity(){return quantity;}
        public String getImageUrl(){return ExternalImageUrlPolicy.isAllowed(imageUrl)?imageUrl:"";}
        public String getProductUrl(){return "/product?handle="+URLEncoder.encode(handle,StandardCharsets.UTF_8);}
        public String getPriceLabel(){return CartRules.money(unitPrice);}
        public String getLineTotalLabel(){return getAvailable()?CartRules.money(CartRules.lineTotal(unitPrice,quantity)):"Item unavailable";}
        public boolean getAvailable(){return visible && CartRules.quantityWithinSample(quantity,sourceQuantity);}
        public boolean getCanUpdate(){return visible;}
        public boolean getHasVerifiedPrice(){return unitPrice!=null;}
        public String getStockLabel(){
            if(!visible) return "Currently unavailable from the Kuwait catalogue";
            if(sourceQuantity==null) return "Stock quantity not supplied (unverified)";
            return (stockVerified?"Available quantity: ":"Unverified sample quantity: ")+sourceQuantity;
        }
    }
    public record Summary(List<Item> items, int distinctLines,int totalQuantity,BigDecimal pricedSubtotal,
                          boolean pendingPrices,boolean unavailable) {
        public List<Item> getItems(){return items;}
        public int getDistinctLines(){return distinctLines;}
        public int getTotalQuantity(){return totalQuantity;}
        public boolean getHasItems(){return !items.isEmpty();}
        public boolean getPendingPrices(){return pendingPrices;}
        public boolean getUnavailable(){return unavailable;}
        public boolean getHasCompleteSubtotal(){return !pendingPrices && !unavailable;}
        public String getSubtotalLabel(){return getHasCompleteSubtotal()?CartRules.money(pricedSubtotal):"Pending verification";}
    }
}

package com.mhamauto.catalog;

import com.mhamauto.media.ExternalImageUrlPolicy;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

/** JDBC row data only: JSPs must use c:out for every user/import-controlled value. */
public final class CatalogModels {
    private CatalogModels() { }
    public record Facet(long id, String name) { public long getId(){return id;} public String getName(){return name;} }
    public record Card(long id, String handle, String title, String vendor, String category,
                       String sku, BigDecimal price, String imageUrl) {
        public String getTitle(){return title;}
        public String getVendor(){return vendor;}
        public String getCategory(){return category;}
        public String getSku(){return sku;}
        public String getHandle(){return handle;}
        public String getUrl() { return "/product?handle=" + URLEncoder.encode(handle, StandardCharsets.UTF_8); }
        public String getPriceLabel() { return price == null ? "Price pending verification" : "KWD " + price.setScale(3).toPlainString(); }
        public String getImageUrl() { return ExternalImageUrlPolicy.isAllowed(imageUrl) ? imageUrl : ""; }
    }
    public record Image(String url, String alt) {
        public String getAlt(){return alt;}
        public String getSafeUrl() { return ExternalImageUrlPolicy.isAllowed(url) ? url : ""; }
    }
    public record Variant(long id, String sku, BigDecimal kwdPrice, Integer sampleQty, boolean stockVerified,
                          String optionSummary) {
        public String getSku(){return sku;}
        public long getId(){return id;}
        public String getOptionSummary(){return optionSummary;}
        public String getPriceLabel() { return kwdPrice == null ? "Price pending verification" : "KWD " + kwdPrice.setScale(3).toPlainString(); }
        public String getStockLabel() { return sampleQty == null ? "Quantity not supplied" :
            (stockVerified ? "Available quantity: " : "Unverified sample quantity: ") + sampleQty; }
    }
    public record Detail(long id, String handle, String title, String vendor, String category, String bodyText,
                         List<Image> images, List<Variant> variants) {
        public String getTitle(){return title;}
        public String getHandle(){return handle;}
        public String getVendor(){return vendor;}
        public String getCategory(){return category;}
        public String getBodyText(){return bodyText;}
        public List<Image> getImages(){return images;}
        public List<Variant> getVariants(){return variants;}
    }
    public record Page(List<Card> cards, long total, int page, int pageSize) {
        public List<Card> getCards(){return cards;}
        public long getTotal(){return total;}
        public int getPage(){return page;}
        public boolean getHasPrevious() { return page > 1; }
        public boolean getHasNext() { return (long)page * pageSize < total; }
        public long getFirstItem() { return total == 0 ? 0 : (long)(page - 1) * pageSize + 1; }
        public long getLastItem() { return Math.min((long)page * pageSize, total); }
    }
}

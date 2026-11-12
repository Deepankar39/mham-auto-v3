package com.mhamauto.admin;
import java.math.BigDecimal;
import java.util.List;

/** JavaBean accessors deliberately supplied for JSP/JSTL EL compatibility. */
public final class AdminModels {
    private AdminModels() { }
    public record Dashboard(long products,long published,long variants,long lowStock,long demoOrders,long pendingOrders,long categories) {
        public long getProducts(){return products;} public long getPublished(){return published;}
        public long getVariants(){return variants;} public long getLowStock(){return lowStock;}
        public long getDemoOrders(){return demoOrders;} public long getPendingOrders(){return pendingOrders;}
        public long getCategories(){return categories;}
    }
    public record Product(long id,String handle,String title,String vendor,String category,String status,boolean published,
                          long variantId,String sku,Integer sourceStock,BigDecimal price,BigDecimal demoPrice,String description,String imageUrl) {
        public long getId(){return id;} public String getHandle(){return handle;} public String getTitle(){return title;}
        public String getVendor(){return vendor;} public String getCategory(){return category;}
        public String getStatus(){return status;} public boolean isPublished(){return published;}
        public boolean getPublished(){return published;} public long getVariantId(){return variantId;}
        public String getSku(){return sku;} public Integer getSourceStock(){return sourceStock;}
        public BigDecimal getPrice(){return price;} public BigDecimal getDemoPrice(){return demoPrice;}
        public String getDescription(){return description;}
        public String getImageUrl(){return com.mhamauto.media.ExternalImageUrlPolicy.isAllowed(imageUrl)?imageUrl:"";}
    }
    public record Facet(long id,String name,boolean managed){public long getId(){return id;} public String getName(){return name;} public boolean getManaged(){return managed;}}
    public record ProductPage(List<Product> items,long total,int page,int size){
        public List<Product> getItems(){return items;} public long getTotal(){return total;}
        public int getPage(){return page;} public int getSize(){return size;}
        public boolean getHasPrevious(){return page>1;}public boolean getHasNext(){return (long)page*size<total;}
    }
    public record Customer(long id,String displayName,String email,String status,String createdAt,long orderCount) {
        public long getId(){return id;} public String getDisplayName(){return displayName;}
        public String getEmail(){return email;} public String getStatus(){return status;}
        public String getCreatedAt(){return createdAt;} public long getOrderCount(){return orderCount;}
    }
    public record CustomerPage(List<Customer> items,long total,int page,int size) {
        public List<Customer> getItems(){return items;} public long getTotal(){return total;}
        public int getPage(){return page;}
        public boolean getHasPrevious(){return page>1;}
        public boolean getHasNext(){return (long)page*size<total;}
    }
    public record Order(long id,String reference,String buyer,String status,BigDecimal total,String createdAt,
                        String recipient,String governorate,String area,String street,String building) {
        public long getId(){return id;}public String getReference(){return reference;}
        public String getBuyer(){return buyer;}public String getStatus(){return status;}
        public BigDecimal getTotal(){return total;}public String getCreatedAt(){return createdAt;}
        public String getRecipient(){return recipient;}public String getGovernorate(){return governorate;}
        public String getArea(){return area;}public String getStreet(){return street;}public String getBuilding(){return building;}
    }
    public record OrderItem(String sku,String title,int quantity,BigDecimal unitPrice,BigDecimal lineTotal,String priceBasis){
        public String getSku(){return sku;} public String getTitle(){return title;}public int getQuantity(){return quantity;}
        public BigDecimal getUnitPrice(){return unitPrice;}public BigDecimal getLineTotal(){return lineTotal;}
        public String getPriceBasis(){return priceBasis;}
    }
    public record OrderPage(List<Order> items,long total,int page,int size){
        public List<Order> getItems(){return items;}public long getTotal(){return total;}
        public int getPage(){return page;}public int getSize(){return size;}
        public boolean getHasPrevious(){return page>1;}public boolean getHasNext(){return (long)page*size<total;}
    }
    public record OrderDetail(Order order,List<OrderItem> lines){public Order getOrder(){return order;}public List<OrderItem> getLines(){return lines;}}
}

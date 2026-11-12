package com.mhamauto.admin;

import com.mhamauto.admin.AdminModels.*;
import com.mhamauto.db.DbPool;
import com.mhamauto.media.ExternalImageUrlPolicy;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.sql.*;
import java.util.*;

/** Admin SQL only: permission decisions are made before entering this DAO. */
public final class AdminDao {
    public static final int PAGE_SIZE=20;
    public record SaveProduct(Long id,String handle,String title,String description,String vendor,Long categoryId,
                              String status,boolean published,String sku,Integer stock,BigDecimal demoPrice,String imageUrl) { }

    public Dashboard dashboard() throws SQLException {
        try(Connection c=DbPool.getConnection(); PreparedStatement p=c.prepareStatement(
          "SELECT (SELECT COUNT(*) FROM products),(SELECT COUNT(*) FROM products WHERE status='active' AND published=TRUE),"+
          "(SELECT COUNT(*) FROM product_variants),(SELECT COUNT(*) FROM inventory WHERE source_quantity<=5),"+
          "(SELECT COUNT(*) FROM demo_orders),(SELECT COUNT(*) FROM demo_orders WHERE status='DEMO_PLACED'),"+
          "(SELECT COUNT(*) FROM categories)")){
            try(ResultSet r=p.executeQuery()){r.next();return new Dashboard(r.getLong(1),r.getLong(2),r.getLong(3),r.getLong(4),r.getLong(5),r.getLong(6),r.getLong(7));}
        }
    }
    public List<Facet> categories() throws SQLException {
        try(Connection c=DbPool.getConnection();PreparedStatement p=c.prepareStatement("SELECT id,name,external_taxonomy FROM categories ORDER BY name,id LIMIT 500")){
            return facets(p);
        }
    }
    private static List<Facet> facets(PreparedStatement p)throws SQLException{
        List<Facet> list=new ArrayList<>();try(ResultSet r=p.executeQuery()){while(r.next())list.add(new Facet(r.getLong(1),r.getString(2),r.getString(3)!=null && r.getString(3).startsWith("ACADEMIC_ADMIN:")));}return List.copyOf(list);
    }
    public ProductPage products(int page,String query) throws SQLException { return products(page,query,""); }
    public ProductPage products(int page,String query,String rawStatus) throws SQLException {
        String q=query==null?"":query.strip();
        if(q.length()>80 || q.chars().anyMatch(Character::isISOControl))throw new IllegalArgumentException("Search is too long or contains invalid characters");
        String status=rawStatus==null?"":rawStatus;
        if(!Set.of("","active","draft","archived").contains(status))throw new IllegalArgumentException("Invalid product filter");
        String where=" WHERE 1=1"+(status.isEmpty()?"":" AND p.status=?")+
          (q.isEmpty()?"":" AND (p.handle LIKE ? ESCAPE '!' OR p.title LIKE ? ESCAPE '!' OR EXISTS (SELECT 1 FROM product_variants sv WHERE sv.product_id=p.id AND sv.sku_raw LIKE ? ESCAPE '!'))");
        String safe=q.replace("!","!!").replace("%","!%").replace("_","!_")+"%";
        try(Connection c=DbPool.getConnection()){
            long total;
            try(PreparedStatement p=c.prepareStatement("SELECT COUNT(*) FROM products p"+where)){
                int i=1;if(!status.isEmpty())p.setString(i++,status);
                if(!q.isEmpty()){p.setString(i++,safe);p.setString(i++,safe);p.setString(i++,safe);}
                try(ResultSet rs=p.executeQuery()){rs.next();total=rs.getLong(1);}
            }
            String sql="SELECT p.id,p.handle,p.title,p.status,p.published,COALESCE(v.name,'') vendor,COALESCE(cat.name,'') category,"+
              "(SELECT m.original_url FROM product_images pi JOIN media_assets m ON m.id=pi.media_id WHERE pi.product_id=p.id ORDER BY pi.source_position IS NULL,pi.source_position,pi.id LIMIT 1) image_url,"+
              "x.id variant_id,x.sku_raw,x.retail_price_kwd,inv.source_quantity,d.assigned_price_kwd " +
              "FROM products p LEFT JOIN vendors v ON p.vendor_id=v.id LEFT JOIN categories cat ON p.category_id=cat.id " +
              "LEFT JOIN product_variants x ON x.id=(SELECT MIN(x2.id) FROM product_variants x2 WHERE x2.product_id=p.id) " +
              "LEFT JOIN inventory inv ON inv.variant_id=x.id LEFT JOIN demo_price_assignments d ON d.variant_id=x.id "+where+
              " ORDER BY p.id DESC LIMIT ? OFFSET ?";
            try(PreparedStatement p=c.prepareStatement(sql)){
                int i=1;if(!status.isEmpty())p.setString(i++,status);
                if(!q.isEmpty()){p.setString(i++,safe);p.setString(i++,safe);p.setString(i++,safe);}
                p.setInt(i++,PAGE_SIZE);p.setInt(i,(page-1)*PAGE_SIZE);
                List<Product> items=new ArrayList<>();try(ResultSet rs=p.executeQuery()){while(rs.next())items.add(mapProduct(rs,null));}
                return new ProductPage(List.copyOf(items),total,page,PAGE_SIZE);
            }
        }
    }
    public Product product(long id) throws SQLException {
        String sql="SELECT p.id,p.handle,p.title,p.body_html,p.status,p.published,COALESCE(v.name,'') vendor,"+
          "COALESCE(cat.name,'') category,"+
          "(SELECT m.original_url FROM product_images pi JOIN media_assets m ON m.id=pi.media_id WHERE pi.product_id=p.id ORDER BY pi.source_position IS NULL,pi.source_position,pi.id LIMIT 1) image_url,"+
          "x.id variant_id,x.sku_raw,x.retail_price_kwd,inv.source_quantity,d.assigned_price_kwd " +
          "FROM products p LEFT JOIN vendors v ON p.vendor_id=v.id LEFT JOIN categories cat ON p.category_id=cat.id " +
          "LEFT JOIN product_variants x ON x.id=(SELECT MIN(x2.id) FROM product_variants x2 WHERE x2.product_id=p.id) " +
          "LEFT JOIN inventory inv ON inv.variant_id=x.id LEFT JOIN demo_price_assignments d ON d.variant_id=x.id WHERE p.id=?";
        try(Connection c=DbPool.getConnection();PreparedStatement p=c.prepareStatement(sql)){
            p.setLong(1,id);try(ResultSet r=p.executeQuery()){return r.next()?mapProduct(r,r.getString("body_html")):null;}
        }
    }
    private static Product mapProduct(ResultSet r,String description)throws SQLException{
        int qty=r.getInt("source_quantity");Integer stock=r.wasNull()?null:qty;
        return new Product(r.getLong("id"),r.getString("handle"),r.getString("title"),r.getString("vendor"),
          r.getString("category"),r.getString("status"),r.getBoolean("published"),r.getLong("variant_id"),
          r.getString("sku_raw"),stock,r.getBigDecimal("retail_price_kwd"),r.getBigDecimal("assigned_price_kwd"),description,r.getString("image_url"));
    }
    private static void audit(Connection c,long actor,String action,String type,long entity,String details)throws SQLException{
        try(PreparedStatement p=c.prepareStatement("INSERT INTO admin_events(actor_user_id,action,entity_type,entity_id,details) VALUES (?,?,?,?,?)")){
            p.setLong(1,actor);p.setString(2,action);p.setString(3,type);p.setLong(4,entity);p.setString(5,details);p.executeUpdate();
        }
    }
    private static long vendorId(Connection c,String name)throws SQLException{
        try(PreparedStatement p=c.prepareStatement("INSERT INTO vendors(name) VALUES(?) ON DUPLICATE KEY UPDATE id=LAST_INSERT_ID(id)")){
            p.setString(1,name);p.executeUpdate();
        }
        try(Statement s=c.createStatement();ResultSet r=s.executeQuery("SELECT LAST_INSERT_ID()")){r.next();return r.getLong(1);}
    }
    private static void checkCategory(Connection c,Long id)throws SQLException{
        if(id==null)return;
        try(PreparedStatement p=c.prepareStatement("SELECT id FROM categories WHERE id=?")){
            p.setLong(1,id);try(ResultSet r=p.executeQuery()){if(!r.next())throw new IllegalArgumentException("Category does not exist");}
        }
    }
    public long createCategory(long actor,String rawName)throws Exception{
        String name=AdminRules.required(rawName,"Category name",190);
        String taxonomy="ACADEMIC_ADMIN:"+name.toLowerCase(Locale.ROOT);
        String hash=HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(taxonomy.getBytes(StandardCharsets.UTF_8)));
        try(Connection c=DbPool.getConnection()){
            c.setAutoCommit(false);
            try{
                long id;
                try(PreparedStatement p=c.prepareStatement("INSERT INTO categories(name,external_taxonomy,taxonomy_sha256) VALUES(?,?,?)",Statement.RETURN_GENERATED_KEYS)){
                    p.setString(1,name);p.setString(2,taxonomy);p.setString(3,hash);p.executeUpdate();
                    try(ResultSet r=p.getGeneratedKeys()){if(!r.next())throw new SQLException("Missing category ID");id=r.getLong(1);}
                }
                audit(c,actor,"CREATE_CATEGORY","CATEGORY",id,"Academic category created");c.commit();return id;
            }catch(Exception e){c.rollback();throw e;}finally{c.setAutoCommit(true);}
        }
    }
    public long saveProduct(long actor,SaveProduct v)throws Exception{
        try(Connection c=DbPool.getConnection()){
            c.setAutoCommit(false);
            try{
                checkCategory(c,v.categoryId());
                long vendor=vendorId(c,v.vendor());
                long productId,variantId;
                if(v.id()==null){
                    try(PreparedStatement p=c.prepareStatement("INSERT INTO products(handle,title,body_html,vendor_id,category_id,status,published) VALUES(?,?,?,?,?,?,?)",Statement.RETURN_GENERATED_KEYS)){
                        p.setString(1,v.handle());p.setString(2,v.title());p.setString(3,AdminRules.escapedDescription(v.description()));
                        p.setLong(4,vendor);if(v.categoryId()==null)p.setNull(5,Types.BIGINT);else p.setLong(5,v.categoryId());
                        p.setString(6,v.status());p.setBoolean(7,v.published());p.executeUpdate();
                        try(ResultSet r=p.getGeneratedKeys()){if(!r.next())throw new SQLException("Missing product ID");productId=r.getLong(1);}
                    }
                    try(PreparedStatement p=c.prepareStatement("INSERT INTO product_variants(product_id,sku_raw,requires_shipping,source_currency) VALUES(?,?,TRUE,NULL)",Statement.RETURN_GENERATED_KEYS)){
                        p.setLong(1,productId);p.setString(2,v.sku());p.executeUpdate();
                        try(ResultSet r=p.getGeneratedKeys()){if(!r.next())throw new SQLException("Missing variant ID");variantId=r.getLong(1);}
                    }
                    try(PreparedStatement p=c.prepareStatement("INSERT INTO inventory(variant_id,source_quantity,verified_live) VALUES(?,?,FALSE)")){
                        p.setLong(1,variantId);if(v.stock()==null)p.setNull(2,Types.INTEGER);else p.setInt(2,v.stock());p.executeUpdate();
                    }
                    try(PreparedStatement p=c.prepareStatement("INSERT INTO variant_market_availability(variant_id,market_key,included) VALUES(?,'KW',TRUE)")){
                        p.setLong(1,variantId);p.executeUpdate();
                    }
                }else{
                    productId=v.id();
                    try(PreparedStatement p=c.prepareStatement("SELECT id FROM products WHERE id=? FOR UPDATE")){
                        p.setLong(1,productId);try(ResultSet r=p.executeQuery()){if(!r.next())throw new IllegalArgumentException("Product not found");}
                    }
                    try(PreparedStatement p=c.prepareStatement("UPDATE products SET title=?,body_html=COALESCE(?,body_html),vendor_id=?,category_id=?,status=?,published=? WHERE id=?")){
                        p.setString(1,v.title());p.setString(2,AdminRules.escapedDescription(v.description()));p.setLong(3,vendor);
                        if(v.categoryId()==null)p.setNull(4,Types.BIGINT);else p.setLong(4,v.categoryId());
                        p.setString(5,v.status());p.setBoolean(6,v.published());p.setLong(7,productId);p.executeUpdate();
                    }
                    // Product handles are stable import keys and must not be renamed on edit.
                    try(PreparedStatement p=c.prepareStatement("SELECT id FROM product_variants WHERE product_id=? ORDER BY id LIMIT 1 FOR UPDATE")){
                        p.setLong(1,productId);try(ResultSet r=p.executeQuery()){if(!r.next())throw new IllegalArgumentException("Product has no variant");variantId=r.getLong(1);}
                    }
                    try(PreparedStatement p=c.prepareStatement("UPDATE product_variants SET sku_raw=? WHERE id=?")){
                        p.setString(1,v.sku());p.setLong(2,variantId);p.executeUpdate();
                    }
                    try(PreparedStatement p=c.prepareStatement("INSERT INTO inventory(variant_id,source_quantity,verified_live) VALUES(?,?,FALSE) "+
                       "ON DUPLICATE KEY UPDATE source_quantity=VALUES(source_quantity),verified_live=FALSE")){
                        p.setLong(1,variantId);if(v.stock()==null)p.setNull(2,Types.INTEGER);else p.setInt(2,v.stock());p.executeUpdate();
                    }
                }
                if(v.demoPrice()!=null){
                    try(PreparedStatement p=c.prepareStatement("INSERT INTO demo_price_assignments(variant_id,assigned_price_kwd,source_note) "+
                      "VALUES(?,?,'ACADEMIC_ADMIN_DEMO') ON DUPLICATE KEY UPDATE assigned_price_kwd=VALUES(assigned_price_kwd),source_note=VALUES(source_note)")){
                        p.setLong(1,variantId);p.setBigDecimal(2,v.demoPrice());p.executeUpdate();
                    }
                    try(PreparedStatement p=c.prepareStatement("UPDATE product_variants SET retail_price_kwd=? WHERE id=?")){
                        p.setBigDecimal(1,v.demoPrice());p.setLong(2,variantId);p.executeUpdate();
                    }
                }
                if(v.id()==null && v.imageUrl()!=null){
                    if(!ExternalImageUrlPolicy.isAllowed(v.imageUrl()))throw new IllegalArgumentException("Use an allowed HTTPS Shopify CDN image URL");
                    String hash=HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(v.imageUrl().getBytes(StandardCharsets.UTF_8)));
                    try(PreparedStatement p=c.prepareStatement("INSERT INTO media_assets(url_sha256,original_url,source_host) VALUES(?,?,'cdn.shopify.com') ON DUPLICATE KEY UPDATE id=LAST_INSERT_ID(id)")){
                        p.setString(1,hash);p.setString(2,v.imageUrl());p.executeUpdate();
                    }
                    long mediaId;try(Statement s=c.createStatement();ResultSet r=s.executeQuery("SELECT LAST_INSERT_ID()")){r.next();mediaId=r.getLong(1);}
                    try(PreparedStatement p=c.prepareStatement("INSERT INTO product_images(product_id,media_id,source_position,alt_text_override) VALUES(?,?,1,?)")){
                        p.setLong(1,productId);p.setLong(2,mediaId);p.setString(3,v.title());p.executeUpdate();
                    }
                }
                audit(c,actor,v.id()==null?"CREATE_PRODUCT":"UPDATE_PRODUCT","PRODUCT",productId,"Sample catalogue and stock edited");
                c.commit();return productId;
            }catch(Exception e){c.rollback();throw e;}finally{c.setAutoCommit(true);}
        }
    }
    /** Read-only customer directory. Email is displayed only inside protected ADMIN routes. */
    public long customerCount() throws SQLException {
        try(Connection c=DbPool.getConnection();PreparedStatement p=c.prepareStatement(
             "SELECT COUNT(*) FROM users u WHERE EXISTS (SELECT 1 FROM user_roles ur JOIN roles r ON r.id=ur.role_id WHERE ur.user_id=u.id AND r.role_code='CUSTOMER')");
            ResultSet r=p.executeQuery()){r.next();return r.getLong(1);}
    }
    public CustomerPage customers(int page,String rawSearch) throws SQLException {
        String q=rawSearch==null?"":rawSearch.strip();
        if(q.length()>80 || q.chars().anyMatch(Character::isISOControl))throw new IllegalArgumentException("Search must be 80 characters or fewer");
        String pattern=q.replace("!","!!").replace("%","!%").replace("_","!_")+"%";
        String where=" WHERE EXISTS (SELECT 1 FROM user_roles ur JOIN roles r ON r.id=ur.role_id WHERE ur.user_id=u.id AND r.role_code='CUSTOMER')"+
                     (q.isBlank()?"":" AND (u.email_normalized LIKE ? ESCAPE '!' OR u.display_name LIKE ? ESCAPE '!')");
        try(Connection c=DbPool.getConnection()){
            long total;
            try(PreparedStatement p=c.prepareStatement("SELECT COUNT(*) FROM users u"+where)){
                if(!q.isEmpty()){p.setString(1,pattern);p.setString(2,pattern);}
                try(ResultSet r=p.executeQuery()){r.next();total=r.getLong(1);}
            }
            String sql="SELECT u.id,u.display_name,u.email_normalized,u.status,u.created_at,"+
                       "(SELECT COUNT(*) FROM demo_orders o WHERE o.user_id=u.id) order_count "+
                       "FROM users u"+where+" ORDER BY u.id DESC LIMIT ? OFFSET ?";
            try(PreparedStatement p=c.prepareStatement(sql)){
                int i=1;if(!q.isEmpty()){p.setString(i++,pattern);p.setString(i++,pattern);}
                p.setInt(i++,PAGE_SIZE);p.setInt(i,(page-1)*PAGE_SIZE);
                List<Customer> rows=new ArrayList<>();
                try(ResultSet r=p.executeQuery()){while(r.next())rows.add(new Customer(r.getLong("id"),r.getString("display_name"),r.getString("email_normalized"),r.getString("status"),r.getTimestamp("created_at").toLocalDateTime().toLocalDate().toString(),r.getLong("order_count")));}
                return new CustomerPage(List.copyOf(rows),total,page,PAGE_SIZE);
            }
        }
    }
    /** Archive preserves product and order history and removes the item from public listings. */
    public void archiveProduct(long actor,long productId) throws SQLException {
        try(Connection c=DbPool.getConnection()){
            c.setAutoCommit(false);
            try{
                try(PreparedStatement p=c.prepareStatement("UPDATE products SET status='archived',published=FALSE WHERE id=?")){
                    p.setLong(1,productId);
                    if(p.executeUpdate()!=1)throw new IllegalArgumentException("Product not found");
                }
                audit(c,actor,"ARCHIVE_PRODUCT","PRODUCT",productId,"Product unpublished and archived");
                c.commit();
            }catch(SQLException|RuntimeException e){c.rollback();throw e;}finally{c.setAutoCommit(true);}
        }
    }
    /** Source-imported categories are immutable through this simplified editor. */
    public void changeCategory(long actor,long id,String action,String rawName) throws SQLException {
        if(!"rename".equals(action)&&!"delete".equals(action))throw new IllegalArgumentException("Invalid category action");
        String name="rename".equals(action)?AdminRules.required(rawName,"Category name",190):null;
        try(Connection c=DbPool.getConnection()){
            c.setAutoCommit(false);
            try{
                String taxonomy;
                try(PreparedStatement p=c.prepareStatement("SELECT external_taxonomy FROM categories WHERE id=? FOR UPDATE")){
                    p.setLong(1,id);try(ResultSet r=p.executeQuery()){
                        if(!r.next())throw new IllegalArgumentException("Category not found");taxonomy=r.getString(1);
                    }
                }
                if(taxonomy==null||!taxonomy.startsWith("ACADEMIC_ADMIN:"))throw new IllegalArgumentException("Imported taxonomy categories cannot be changed here");
                if("delete".equals(action)){
                    try(PreparedStatement p=c.prepareStatement("SELECT COUNT(*) FROM products WHERE category_id=?")){
                        p.setLong(1,id);try(ResultSet r=p.executeQuery()){r.next();if(r.getLong(1)>0)throw new IllegalArgumentException("Remove product assignments before deleting this category");}
                    }
                    try(PreparedStatement p=c.prepareStatement("DELETE FROM categories WHERE id=?")){p.setLong(1,id);p.executeUpdate();}
                }else{
                    try(PreparedStatement p=c.prepareStatement("UPDATE categories SET name=? WHERE id=?")){p.setString(1,name);p.setLong(2,id);p.executeUpdate();}
                }
                audit(c,actor,"delete".equals(action)?"DELETE_CATEGORY":"RENAME_CATEGORY","CATEGORY",id,"Administrator-created category updated");
                c.commit();
            }catch(SQLException|RuntimeException e){c.rollback();throw e;}finally{c.setAutoCommit(true);}
        }
    }
    public OrderPage orders(int page,String status) throws SQLException {
        String filter=status==null||status.isBlank()?null:status;
        if(filter!=null && !Set.of("DEMO_PLACED","DEMO_REVIEWED","DEMO_CANCELLED").contains(filter))throw new IllegalArgumentException("Invalid order filter");
        try(Connection c=DbPool.getConnection()){
            long total;
            try(PreparedStatement p=c.prepareStatement("SELECT COUNT(*) FROM demo_orders"+(filter==null?"":" WHERE status=?"))){
                if(filter!=null)p.setString(1,filter);
                try(ResultSet r=p.executeQuery()){r.next();total=r.getLong(1);}
            }
            String sql="SELECT o.id,o.order_reference,u.email_normalized buyer,o.status,o.total_kwd,o.created_at,o.recipient_name,o.governorate,o.area,o.street,o.building " +
     "FROM demo_orders o JOIN users u ON u.id=o.user_id "+(filter==null?"":"WHERE o.status=? ")+"ORDER BY o.id DESC LIMIT ? OFFSET ?";
            try(PreparedStatement p=c.prepareStatement(sql)){
                int i=1;if(filter!=null)p.setString(i++,filter);p.setInt(i++,PAGE_SIZE);p.setInt(i,(page-1)*PAGE_SIZE);
                List<Order> list=new ArrayList<>();try(ResultSet r=p.executeQuery()){while(r.next())list.add(mapOrder(r));}
                return new OrderPage(List.copyOf(list),total,page,PAGE_SIZE);
            }
        }
    }
    private static Order mapOrder(ResultSet r)throws SQLException {
        return new Order(r.getLong("id"),r.getString("order_reference"),r.getString("buyer"),r.getString("status"),r.getBigDecimal("total_kwd"),
          r.getTimestamp("created_at").toLocalDateTime().toString().replace('T',' '),r.getString("recipient_name"),r.getString("governorate"),
          r.getString("area"),r.getString("street"),r.getString("building"));
    }
    public OrderDetail order(long id)throws SQLException{
        try(Connection c=DbPool.getConnection()){
            Order header;
            try(PreparedStatement p=c.prepareStatement("SELECT o.id,o.order_reference,u.email_normalized buyer,o.status,o.total_kwd,o.created_at,o.recipient_name,o.governorate,o.area,o.street,o.building "+
  "FROM demo_orders o JOIN users u ON u.id=o.user_id WHERE o.id=?")){
                p.setLong(1,id);try(ResultSet r=p.executeQuery()){if(!r.next())return null;header=mapOrder(r);}
            }
            List<OrderItem> items=new ArrayList<>();
            try(PreparedStatement p=c.prepareStatement("SELECT sku_snapshot,title_snapshot,quantity,unit_price_kwd,line_total_kwd,price_basis FROM demo_order_items WHERE order_id=? ORDER BY id LIMIT 100")){
                p.setLong(1,id);try(ResultSet r=p.executeQuery()){while(r.next())items.add(new OrderItem(r.getString(1),r.getString(2),r.getInt(3),r.getBigDecimal(4),r.getBigDecimal(5),r.getString(6)));}
            }
            return new OrderDetail(header,List.copyOf(items));
        }
    }
    public void changeOrderStatus(long actor,long orderId,String next)throws SQLException {
        try(Connection c=DbPool.getConnection()){
            c.setAutoCommit(false);
            try{
                String old;
                try(PreparedStatement p=c.prepareStatement("SELECT status FROM demo_orders WHERE id=? FOR UPDATE")){
                    p.setLong(1,orderId);try(ResultSet r=p.executeQuery()){if(!r.next())throw new IllegalArgumentException("Order not found");old=r.getString(1);}
                }
                AdminRules.nextOrderStatus(old,next);
                if(!old.equals(next)){
                    try(PreparedStatement p=c.prepareStatement("UPDATE demo_orders SET status=? WHERE id=?")){
                        p.setString(1,next);p.setLong(2,orderId);p.executeUpdate();
                    }
                    audit(c,actor,"ORDER_STATUS","DEMO_ORDER",orderId,old+" -> "+next);
                }
                c.commit();
            }catch(SQLException|RuntimeException e){c.rollback();throw e;}finally{c.setAutoCommit(true);}
        }
    }
}

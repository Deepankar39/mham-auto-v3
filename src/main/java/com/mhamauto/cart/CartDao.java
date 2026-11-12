package com.mhamauto.cart;

import com.mhamauto.cart.CartModels.*;
import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/** JDBC queries never interpolate user data into SQL. Transaction locking is owned by CartService. */
public final class CartDao {
    public long lockedCart(Connection c,long userId) throws SQLException {
        try(PreparedStatement p=c.prepareStatement("INSERT IGNORE INTO carts(user_id) VALUES(?)")) {p.setLong(1,userId);p.executeUpdate();}
        try(PreparedStatement p=c.prepareStatement("SELECT id FROM carts WHERE user_id=? FOR UPDATE")) {
            p.setLong(1,userId);
            try(ResultSet r=p.executeQuery()) {if(!r.next()) throw new SQLException("Cart was not found");return r.getLong(1);}
        }
    }
    /** This query also locks candidate product/variant/inventory while a cart operation is processed. */
    public Integer sourceQuantityForMutation(Connection c,long variantId) throws SQLException {
        String sql="SELECT x.id,p.status,p.published,ma.included,i.source_quantity " +
            "FROM product_variants x JOIN products p ON p.id=x.product_id " +
            "LEFT JOIN variant_market_availability ma ON ma.variant_id=x.id AND ma.market_key='KW' " +
            "LEFT JOIN inventory i ON i.variant_id=x.id WHERE x.id=? FOR UPDATE";
        try(PreparedStatement p=c.prepareStatement(sql)) {
            p.setLong(1,variantId);
            try(ResultSet r=p.executeQuery()) {
                if(!r.next() || !"active".equals(r.getString("status")) || !r.getBoolean("published") ||
                        (r.getObject("included")!=null && !r.getBoolean("included")))
                    throw new CartConflict("This part is no longer available in the Kuwait demo catalogue");
                int n=r.getInt("source_quantity"); return r.wasNull()?null:n;
            }
        }
    }
    public int distinctLineCount(Connection c,long cartId) throws SQLException {
        try(PreparedStatement p=c.prepareStatement("SELECT COUNT(*) FROM cart_items WHERE cart_id=?")) {
            p.setLong(1,cartId);
            try(ResultSet r=p.executeQuery()) {r.next();return r.getInt(1);}
        }
    }
    public int existingQuantity(Connection c,long cartId,long variantId) throws SQLException {
        try(PreparedStatement p=c.prepareStatement("SELECT quantity FROM cart_items WHERE cart_id=? AND variant_id=?")) {
            p.setLong(1,cartId);p.setLong(2,variantId);
            try(ResultSet r=p.executeQuery()) {return r.next()?r.getInt(1):0;}
        }
    }
    public void upsert(Connection c,long cartId,long variantId,int quantity) throws SQLException {
        try(PreparedStatement p=c.prepareStatement("INSERT INTO cart_items(cart_id,variant_id,quantity) VALUES(?,?,?) " +
                "ON DUPLICATE KEY UPDATE quantity=VALUES(quantity),updated_at=CURRENT_TIMESTAMP")) {
            p.setLong(1,cartId);p.setLong(2,variantId);p.setInt(3,quantity);p.executeUpdate();
        }
    }
    public int updateExisting(Connection c,long cartId,long variantId,int quantity) throws SQLException {
        try(PreparedStatement p=c.prepareStatement("UPDATE cart_items SET quantity=? WHERE cart_id=? AND variant_id=?")) {
            p.setInt(1,quantity);p.setLong(2,cartId);p.setLong(3,variantId);return p.executeUpdate();
        }
    }
    public void remove(Connection c,long cartId,long variantId) throws SQLException {
        try(PreparedStatement p=c.prepareStatement("DELETE FROM cart_items WHERE cart_id=? AND variant_id=?")) {
            p.setLong(1,cartId);p.setLong(2,variantId);p.executeUpdate();
        }
    }
    public void clear(Connection c,long cartId) throws SQLException {
        try(PreparedStatement p=c.prepareStatement("DELETE FROM cart_items WHERE cart_id=?")) {p.setLong(1,cartId);p.executeUpdate();}
    }
    public int count(Connection c,long userId) throws SQLException {
        try(PreparedStatement p=c.prepareStatement("SELECT COALESCE(SUM(i.quantity),0) FROM carts x " +
                "JOIN cart_items i ON i.cart_id=x.id WHERE x.user_id=?")) {
            p.setLong(1,userId);
            try(ResultSet r=p.executeQuery()) {r.next();return r.getInt(1);}
        }
    }
    public Summary read(Connection c,long userId) throws SQLException {
        String sql="SELECT ci.variant_id,ci.quantity,p.handle,p.title,x.sku_raw,x.retail_price_kwd," +
           "p.published,p.status,ma.included,i.source_quantity,i.verified_live," +
           "(SELECT m.original_url FROM product_images pi JOIN media_assets m ON m.id=pi.media_id " +
           " WHERE pi.product_id=p.id ORDER BY pi.source_position IS NULL,pi.source_position,pi.id LIMIT 1) AS image_url " +
           "FROM carts ca JOIN cart_items ci ON ci.cart_id=ca.id JOIN product_variants x ON x.id=ci.variant_id " +
           "JOIN products p ON p.id=x.product_id LEFT JOIN inventory i ON i.variant_id=x.id " +
           "LEFT JOIN variant_market_availability ma ON ma.variant_id=x.id AND ma.market_key='KW' " +
           "WHERE ca.user_id=? ORDER BY ci.created_at,ci.id LIMIT 100";
        List<Item> items=new ArrayList<>();int units=0;boolean unpriced=false,unavailable=false;
        BigDecimal total=new BigDecimal("0.000");
        try(PreparedStatement ps=c.prepareStatement(sql)) {
            ps.setLong(1,userId);
            try(ResultSet r=ps.executeQuery()) {
                while(r.next()) {
                    int qty=r.getInt("quantity");units+=qty;
                    int src=r.getInt("source_quantity");Integer sourceQty=r.wasNull()?null:src;
                    boolean eligible="active".equals(r.getString("status")) && r.getBoolean("published") &&
                        (r.getObject("included")==null || r.getBoolean("included"));
                    BigDecimal price=r.getBigDecimal("retail_price_kwd");
                    if(price==null) unpriced=true;
                    if(!eligible || !CartRules.quantityWithinSample(qty,sourceQty)) unavailable=true;
                    else if(price!=null) total=total.add(CartRules.lineTotal(price,qty));
                    items.add(new Item(r.getLong("variant_id"),r.getString("handle"),r.getString("title"),
                        r.getString("sku_raw"),"",r.getString("image_url"),qty,price,sourceQty,
                        r.getBoolean("verified_live"),eligible));
                }
            }
        }
        return new Summary(List.copyOf(items),items.size(),units,total,unpriced,unavailable);
    }
}

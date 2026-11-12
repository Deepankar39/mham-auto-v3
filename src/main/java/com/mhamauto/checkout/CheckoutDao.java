package com.mhamauto.checkout;

import com.mhamauto.checkout.AddressRules.AddressData;
import com.mhamauto.checkout.CheckoutModels.Order;
import com.mhamauto.checkout.CheckoutModels.OrderLine;
import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public final class CheckoutDao {
    /** Idempotency scoped to authenticated user, not just token. */
    public String existingReference(Connection c,long user,String token) throws SQLException {
        try(PreparedStatement p=c.prepareStatement("SELECT order_reference FROM demo_orders WHERE user_id=? AND checkout_token=?")){
            p.setLong(1,user);p.setString(2,token);try(ResultSet r=p.executeQuery()){return r.next()?r.getString(1):null;}
        }
    }
    public record CheckoutItem(long variant,String sku,String title,int qty,BigDecimal price,BigDecimal line,String priceBasis) { }
    /** Lock exact current product/variant rows after the user's cart row is locked. */
    public List<CheckoutItem> lockedItems(Connection c,long user) throws SQLException {
        String sql="SELECT ci.variant_id,ci.quantity,v.sku_raw,v.retail_price_kwd,p.title,p.status,p.published,ma.included,i.source_quantity,d.variant_id AS demo_variant_id " +
            "FROM carts ca JOIN cart_items ci ON ci.cart_id=ca.id " +
            "JOIN product_variants v ON v.id=ci.variant_id JOIN products p ON p.id=v.product_id " +
            "LEFT JOIN variant_market_availability ma ON ma.variant_id=v.id AND ma.market_key='KW' " +
            "LEFT JOIN inventory i ON i.variant_id=v.id " +
            "LEFT JOIN demo_price_assignments d ON d.variant_id=v.id AND d.assigned_price_kwd=v.retail_price_kwd " +
            "WHERE ca.user_id=? ORDER BY ci.variant_id FOR UPDATE";
        List<CheckoutItem> items=new ArrayList<>();
        try(PreparedStatement p=c.prepareStatement(sql)) {
            p.setLong(1,user);try(ResultSet r=p.executeQuery()) {
                while(r.next()) {
                    if(items.size()>=100)throw new CheckoutConflict("Cart is too large for a demo order");
                    int qty=r.getInt("quantity");int source=r.getInt("source_quantity");boolean known=!r.wasNull();
                    if(qty<1 || qty>20 || (known && qty>source))throw new CheckoutConflict("Review the quantity or sample stock in your cart");
                    if(!"active".equals(r.getString("status")) || !r.getBoolean("published") ||
                      (r.getObject("included")!=null && !r.getBoolean("included")))
                        throw new CheckoutConflict("One or more products are not available for the Kuwait demo");
                    BigDecimal value=r.getBigDecimal("retail_price_kwd");
                    if(value==null)throw new CheckoutConflict("Checkout needs verified or explicitly assigned demo KWD prices for every item");
                    BigDecimal price=CheckoutRules.price(value);
                    items.add(new CheckoutItem(r.getLong("variant_id"),r.getString("sku_raw"),r.getString("title"),qty,price,CheckoutRules.line(price,qty),r.getObject("demo_variant_id")==null?"KWD_CATALOGUE":"SYNTHETIC_DEMO"));
                }
            }
        }
        if(items.isEmpty())throw new CheckoutConflict("Cart is empty. Add a part before checkout");
        return List.copyOf(items);
    }
    public long insertOrder(Connection c,long user,String ref,String token,AddressData a,BigDecimal subtotal) throws SQLException {
        String sql="INSERT INTO demo_orders(user_id,order_reference,checkout_token,recipient_name,phone,governorate,area,block_name,street,building,floor_detail,extra_directions,subtotal_kwd,total_kwd) " +
            "VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
        try(PreparedStatement p=c.prepareStatement(sql,Statement.RETURN_GENERATED_KEYS)) {
            p.setLong(1,user);p.setString(2,ref);p.setString(3,token);p.setString(4,a.recipientName());p.setString(5,a.phone());
            p.setString(6,a.governorate());p.setString(7,a.area());p.setString(8,a.block());p.setString(9,a.street());p.setString(10,a.building());
            p.setString(11,a.floorDetail());p.setString(12,a.directions());p.setBigDecimal(13,subtotal);p.setBigDecimal(14,subtotal);p.executeUpdate();
            try(ResultSet r=p.getGeneratedKeys()){if(!r.next())throw new SQLException("Missing generated order ID");return r.getLong(1);}
        }
    }
    public void insertItems(Connection c,long orderId,List<CheckoutItem> items) throws SQLException {
        try(PreparedStatement p=c.prepareStatement("INSERT INTO demo_order_items(order_id,variant_id,sku_snapshot,title_snapshot,quantity,unit_price_kwd,line_total_kwd,price_basis) VALUES(?,?,?,?,?,?,?,?)")){
            for(CheckoutItem line:items){p.setLong(1,orderId);p.setLong(2,line.variant());p.setString(3,line.sku());p.setString(4,line.title());
                p.setInt(5,line.qty());p.setBigDecimal(6,line.price());p.setBigDecimal(7,line.line());p.setString(8,line.priceBasis());p.addBatch();}
            p.executeBatch();
        }
    }
    public Order findOrder(Connection c,long user,String ref) throws SQLException {
        String sql="SELECT id,order_reference,status,created_at,recipient_name,phone,governorate,area,block_name,street,building,floor_detail,extra_directions,subtotal_kwd,total_kwd " +
            "FROM demo_orders WHERE user_id=? AND order_reference=?";
        try(PreparedStatement p=c.prepareStatement(sql)) {
            p.setLong(1,user);p.setString(2,ref);
            try(ResultSet r=p.executeQuery()) {
                if(!r.next())return null;
                long id=r.getLong("id");AddressData a=new AddressData(r.getString("recipient_name"),r.getString("phone"),r.getString("governorate"),r.getString("area"),
                    r.getString("block_name"),r.getString("street"),r.getString("building"),r.getString("floor_detail"),r.getString("extra_directions"));
                return new Order(id,r.getString("order_reference"),r.getString("status"),r.getTimestamp("created_at").toLocalDateTime().toString(),a,
                    r.getBigDecimal("subtotal_kwd"),r.getBigDecimal("total_kwd"),loadLines(c,id));
            }
        }
    }
    private List<OrderLine> loadLines(Connection c,long order) throws SQLException {
        try(PreparedStatement p=c.prepareStatement("SELECT sku_snapshot,title_snapshot,quantity,unit_price_kwd,line_total_kwd,price_basis FROM demo_order_items WHERE order_id=? ORDER BY id")){
            p.setLong(1,order);try(ResultSet r=p.executeQuery()){List<OrderLine> out=new ArrayList<>();while(r.next())out.add(new OrderLine(r.getString(1),r.getString(2),r.getInt(3),r.getBigDecimal(4),r.getBigDecimal(5),r.getString(6)));return List.copyOf(out);}
        }
    }
}

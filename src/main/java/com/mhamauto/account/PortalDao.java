package com.mhamauto.account;

import com.mhamauto.account.PortalModels.*;
import com.mhamauto.auth.AuthDao;
import com.mhamauto.auth.AuthPrincipal;
import com.mhamauto.cart.CartDao;
import com.mhamauto.checkout.CheckoutDao;
import com.mhamauto.checkout.CheckoutModels.Order;
import com.mhamauto.db.DbPool;
import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/** All reads are scoped by authenticated user ID; never trust an incoming owner ID. */
public final class PortalDao {
    // Fixed SQL with placeholders, no dynamic column names or interpolated request parameters.
    private static final String ORDER_CARDS="SELECT o.order_reference,o.status,o.created_at,o.total_kwd, " +
        "(SELECT COUNT(*) FROM demo_order_items li WHERE li.order_id=o.id) AS line_count, " +
        "(SELECT COALESCE(SUM(li.quantity),0) FROM demo_order_items li WHERE li.order_id=o.id) AS units " +
        "FROM demo_orders o WHERE o.user_id=? ORDER BY o.created_at DESC,o.id DESC LIMIT ? OFFSET ?";
    public Dashboard dashboard(long user) throws SQLException {
        try(Connection c=DbPool.getConnection()) {
            long orders=scalar(c,"SELECT COUNT(*) FROM demo_orders WHERE user_id=?",user);
            long addresses=scalar(c,"SELECT COUNT(*) FROM customer_addresses WHERE user_id=?",user);
            BigDecimal total=total(c,user);
            int units=new CartDao().count(c,user);
            return new Dashboard(orders,addresses,units,total,orderCards(c,user,5,0));
        }
    }
    public OrderPage orders(long user,int page) throws SQLException {
        int offset=PortalRules.offset(page);
        try(Connection c=DbPool.getConnection()) {
            long count=scalar(c,"SELECT COUNT(*) FROM demo_orders WHERE user_id=?",user);
            long pageCount=Math.max(1,(count+PortalRules.PAGE_SIZE-1)/PortalRules.PAGE_SIZE);
            // Reject pages beyond available records, except the first page of an empty account.
            if(page>pageCount)throw new IllegalArgumentException("Order history page not found");
            return new OrderPage(orderCards(c,user,PortalRules.PAGE_SIZE,offset),page,(int)Math.min(pageCount,5000),count);
        }
    }
    public Order order(long user,String reference) throws SQLException {
        PortalRules.reference(reference);
        try(Connection c=DbPool.getConnection()){return new CheckoutDao().findOrder(c,user,reference);}
    }
    public AuthPrincipal updateDisplayName(long user,String requestedName) throws SQLException {
        String normalized=PortalRules.displayName(requestedName);
        try(Connection c=DbPool.getConnection()) {
            try(PreparedStatement p=c.prepareStatement("UPDATE users SET display_name=? WHERE id=? AND status='ACTIVE'")) {
                p.setString(1,normalized);p.setLong(2,user);
                if(p.executeUpdate()!=1)throw new IllegalStateException("Account is no longer available");
            }
            AuthDao.Account account=new AuthDao().byId(c,user);
            if(account==null || !account.active())throw new IllegalStateException("Account is no longer available");
            return account.principal();
        }
    }
    private static long scalar(Connection c,String sql,long user) throws SQLException {
        try(PreparedStatement p=c.prepareStatement(sql)) {
            p.setLong(1,user);try(ResultSet r=p.executeQuery()){r.next();return r.getLong(1);}
        }
    }
    private static BigDecimal total(Connection c,long user) throws SQLException {
        try(PreparedStatement p=c.prepareStatement("SELECT COALESCE(SUM(total_kwd),0.000) FROM demo_orders WHERE user_id=?")){
            p.setLong(1,user);try(ResultSet r=p.executeQuery()){r.next();return r.getBigDecimal(1);}
        }
    }
    private static List<OrderCard> orderCards(Connection c,long user,int limit,int offset) throws SQLException {
        try(PreparedStatement p=c.prepareStatement(ORDER_CARDS)){
            p.setLong(1,user);p.setInt(2,limit);p.setInt(3,offset);
            try(ResultSet r=p.executeQuery()) {
                List<OrderCard> cards=new ArrayList<>();
                while(r.next())cards.add(new OrderCard(r.getString("order_reference"),r.getString("status"),
                    r.getTimestamp("created_at").toLocalDateTime().toString(),r.getInt("line_count"),r.getInt("units"),r.getBigDecimal("total_kwd")));
                return List.copyOf(cards);
            }
        }
    }
}

package com.mhamauto.checkout;

import com.mhamauto.cart.CartDao;
import com.mhamauto.cart.CartModels;
import com.mhamauto.db.DbPool;
import com.mhamauto.checkout.CheckoutModels.Address;
import com.mhamauto.checkout.CheckoutModels.Order;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

/** Single commit for demo order header, immutable line snapshots and cart cleanup. */
public final class CheckoutService {
    private final CartDao cartDao=new CartDao();
    private final AddressDao addresses=new AddressDao();
    private final CheckoutDao orders=new CheckoutDao();
    public CartModels.Summary preview(long user) throws SQLException {try(Connection c=DbPool.getConnection()){return cartDao.read(c,user);}}
    public List<Address> addresses(long user) throws SQLException {try(Connection c=DbPool.getConnection()){return addresses.list(c,user);}}
    public Order order(long user,String reference) throws SQLException {try(Connection c=DbPool.getConnection()){return orders.findOrder(c,user,reference);}}
    public boolean alreadyPlaced(long user,String token) throws SQLException {
        try(Connection c=DbPool.getConnection()){return orders.existingReference(c,user,CheckoutRules.token(token))!=null;}
    }
    public String checkout(long user,long addressId,String token,boolean validSessionNonce) throws SQLException {
        if(user<1)throw new IllegalArgumentException("Invalid customer");
        CheckoutRules.token(token);
        try(Connection c=DbPool.getConnection()){
            c.setAutoCommit(false);
            try{
                // This is the same row-lock order as CartService; serializes cart changes and concurrent checkout attempts.
                long cartId=cartDao.lockedCart(c,user);
                String previous=orders.existingReference(c,user,token);
                if(previous!=null){c.commit();return previous;}
                if(!validSessionNonce)throw new CheckoutConflict("Checkout form has expired. Reload and try again");
                Address address=addresses.find(c,user,addressId);
                if(address==null)throw new CheckoutConflict("Choose an address saved in your own account");
                List<CheckoutDao.CheckoutItem> items=orders.lockedItems(c,user);
                BigDecimal subtotal=new BigDecimal("0.000");
                for(var item:items) subtotal=subtotal.add(item.line());
                CheckoutRules.price(subtotal);
                String reference="DEMO-"+UUID.randomUUID().toString().replace("-","").substring(0,20).toUpperCase(java.util.Locale.ROOT);
                long id=orders.insertOrder(c,user,reference,token,address.data(),subtotal);
                orders.insertItems(c,id,items);
                cartDao.clear(c,cartId); // no real inventory decrement / no external calls
                c.commit();return reference;
            }catch(SQLException|RuntimeException e){try{c.rollback();}catch(SQLException rollback){e.addSuppressed(rollback);}throw e;}
            finally{c.setAutoCommit(true);}
        }
    }
}

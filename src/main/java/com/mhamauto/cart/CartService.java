package com.mhamauto.cart;

import com.mhamauto.cart.CartModels.Summary;
import com.mhamauto.db.DbPool;
import java.sql.Connection;
import java.sql.SQLException;

/** Serializes per-user cart mutations on the cart row. Rolls back on any failure. */
public final class CartService {
    private final CartDao dao=new CartDao();
    public Summary read(long userId) throws SQLException {
        try(Connection c=DbPool.getConnection()) {return dao.read(c,userId);}
    }
    public int count(long userId) throws SQLException {
        try(Connection c=DbPool.getConnection()) {return dao.count(c,userId);}
    }
    public void change(long userId,String action,long variantId,int quantity) throws SQLException {
        if(userId<1) throw new IllegalArgumentException("Invalid user");
        if(!action.equals("add") && !action.equals("update") && !action.equals("remove") && !action.equals("clear"))
            throw new IllegalArgumentException("Unknown cart action");
        try(Connection c=DbPool.getConnection()) {
            c.setAutoCommit(false);
            try {
                long cartId=dao.lockedCart(c,userId);
                switch(action) {
                    case "add" -> {
                        int existing=dao.existingQuantity(c,cartId,variantId);
                        if(existing==0 && dao.distinctLineCount(c,cartId)>=100)
                            throw new CartConflict("Cart supports at most 100 different parts");
                        int combined;
                        try {combined=Math.addExact(existing,quantity);}
                        catch(ArithmeticException ex) {throw new CartConflict("Quantity exceeds the per-item limit of 20");}
                        validateQuantity(c,variantId,combined);
                        dao.upsert(c,cartId,variantId,combined);
                    }
                    case "update" -> {
                        validateQuantity(c,variantId,quantity);
                        if(dao.existingQuantity(c,cartId,variantId)==0)
                            throw new CartConflict("This item is not in your cart");
                        dao.updateExisting(c,cartId,variantId,quantity);
                    }
                    case "remove" -> dao.remove(c,cartId,variantId);
                    case "clear" -> dao.clear(c,cartId);
                    default -> throw new IllegalArgumentException("Unexpected cart action");
                }
                c.commit();
            } catch (SQLException | RuntimeException problem) {
                try {c.rollback();} catch(SQLException rollback) {problem.addSuppressed(rollback);}
                throw problem;
            } finally {c.setAutoCommit(true);}
        }
    }
    private void validateQuantity(Connection c,long variantId,int quantity) throws SQLException {
        if(quantity<1 || quantity>CartRules.MAX_QUANTITY) throw new CartConflict("Quantity exceeds the per-item limit of 20");
        Integer sourceQuantity=dao.sourceQuantityForMutation(c,variantId);
        if(!CartRules.quantityWithinSample(quantity,sourceQuantity))
            throw new CartConflict("Quantity exceeds the current source inventory count");
    }
}

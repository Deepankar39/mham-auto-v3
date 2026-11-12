package com.mhamauto.checkout;

import com.mhamauto.checkout.AddressRules.AddressData;
import com.mhamauto.checkout.CheckoutModels.Address;
import com.mhamauto.db.DbPool;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public final class AddressService {
    private final AddressDao dao=new AddressDao();
    public List<Address> list(long user) throws SQLException {try(Connection c=DbPool.getConnection()){return dao.list(c,user);}}
    public Address find(long user,long id) throws SQLException {try(Connection c=DbPool.getConnection()){return dao.find(c,user,id);}}
    public void save(long user,Long addressId,AddressData data) throws SQLException {
        try(Connection c=DbPool.getConnection()) {
            c.setAutoCommit(false);
            try{dao.save(c,user,addressId,data);c.commit();}
            catch(SQLException|RuntimeException e){try{c.rollback();}catch(SQLException rollback){e.addSuppressed(rollback);}throw e;}
            finally{c.setAutoCommit(true);}
        }
    }
    public void delete(long user,long addressId) throws SQLException {
        try(Connection c=DbPool.getConnection()) {
            c.setAutoCommit(false);
            try{dao.delete(c,user,addressId);c.commit();}
            catch(SQLException|RuntimeException e){try{c.rollback();}catch(SQLException rollback){e.addSuppressed(rollback);}throw e;}
            finally{c.setAutoCommit(true);}
        }
    }
}

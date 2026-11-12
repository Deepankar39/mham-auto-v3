package com.mhamauto.checkout;

import com.mhamauto.checkout.AddressRules.AddressData;
import com.mhamauto.checkout.CheckoutModels.Address;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/** Every lookup and mutation is restricted to the authenticated customer ID. */
public final class AddressDao {
    private static final String FIELDS="id,recipient_name,phone,governorate,area,block_name,street,building,floor_detail,extra_directions";
    public List<Address> list(Connection c,long user) throws SQLException {
        try(PreparedStatement p=c.prepareStatement("SELECT "+FIELDS+" FROM customer_addresses WHERE user_id=? ORDER BY id DESC LIMIT 11")) {
            p.setLong(1,user);try(ResultSet rs=p.executeQuery()) {List<Address> result=new ArrayList<>();while(rs.next())result.add(map(rs));return result;}
        }
    }
    public Address find(Connection c,long user,long id) throws SQLException {
        try(PreparedStatement p=c.prepareStatement("SELECT "+FIELDS+" FROM customer_addresses WHERE user_id=? AND id=?")) {
            p.setLong(1,user);p.setLong(2,id);try(ResultSet r=p.executeQuery()){return r.next()?map(r):null;}
        }
    }
    public void lockUser(Connection c,long user) throws SQLException {
        try(PreparedStatement p=c.prepareStatement("SELECT id FROM users WHERE id=? AND status='ACTIVE' FOR UPDATE")) {
            p.setLong(1,user);try(ResultSet r=p.executeQuery()){if(!r.next())throw new CheckoutConflict("Customer account is unavailable");}
        }
    }
    public void save(Connection c,long user,Long id,AddressData a) throws SQLException {
        lockUser(c,user); // serialize address limit and edits from multiple tabs
        if(id==null) {
            try(PreparedStatement check=c.prepareStatement("SELECT COUNT(*) FROM customer_addresses WHERE user_id=?")) {
                check.setLong(1,user);try(ResultSet r=check.executeQuery()) {r.next();if(r.getInt(1)>=10)throw new CheckoutConflict("You can save at most ten addresses");}
            }
            try(PreparedStatement p=c.prepareStatement("INSERT INTO customer_addresses(user_id,recipient_name,phone,governorate,area,block_name,street,building,floor_detail,extra_directions) VALUES(?,?,?,?,?,?,?,?,?,?)")) {
                p.setLong(1,user);bind(p,2,a);p.executeUpdate();
            }
        } else {
            try(PreparedStatement p=c.prepareStatement("UPDATE customer_addresses SET recipient_name=?,phone=?,governorate=?,area=?,block_name=?,street=?,building=?,floor_detail=?,extra_directions=? WHERE user_id=? AND id=?")) {
                bind(p,1,a);p.setLong(10,user);p.setLong(11,id);
                if(p.executeUpdate()!=1)throw new CheckoutConflict("Address not found in your account");
            }
        }
    }
    public void delete(Connection c,long user,long id) throws SQLException {
        lockUser(c,user);
        try(PreparedStatement p=c.prepareStatement("DELETE FROM customer_addresses WHERE id=? AND user_id=?")) {
            p.setLong(1,id);p.setLong(2,user);if(p.executeUpdate()!=1)throw new CheckoutConflict("Address not found in your account");
        }
    }
    private static void bind(PreparedStatement p,int i,AddressData a) throws SQLException {
        p.setString(i,a.recipientName());p.setString(i+1,a.phone());p.setString(i+2,a.governorate());p.setString(i+3,a.area());
        p.setString(i+4,a.block());p.setString(i+5,a.street());p.setString(i+6,a.building());
        p.setString(i+7,a.floorDetail());p.setString(i+8,a.directions());
    }
    private static Address map(ResultSet r) throws SQLException {
        return new Address(r.getLong("id"),new AddressData(r.getString("recipient_name"),r.getString("phone"),r.getString("governorate"),
            r.getString("area"),r.getString("block_name"),r.getString("street"),r.getString("building"),
            r.getString("floor_detail"),r.getString("extra_directions")));
    }
}

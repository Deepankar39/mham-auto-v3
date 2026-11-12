package com.mhamauto.trade;

import com.mhamauto.db.DbPool;
import com.mhamauto.trade.TradeModels.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/** Owner-scoped reads, atomic application creation and review with immutable event history. */
public final class TradeDao {
    private static final String FIELDS="a.id,a.user_id,a.company_name,a.contact_name,a.contact_phone,a.business_type,"+
        "a.governorate,a.business_area,a.applicant_note,a.status,a.created_at,a.updated_at,"+
        "u.email_normalized AS applicant_email,a.reviewed_at,a.admin_note";
    private static final String JOIN=" FROM business_applications a JOIN users u ON u.id=a.user_id ";

    public Application mine(long userId) throws SQLException {
        try(Connection c=DbPool.getConnection();PreparedStatement p=c.prepareStatement(
                "SELECT "+FIELDS+JOIN+" WHERE a.user_id=?")){
            p.setLong(1,userId);
            try(ResultSet rs=p.executeQuery()){return rs.next()?map(rs,false):null;}
        }
    }
    public long submit(long userId,TradeRules.Input input) throws SQLException {
        try(Connection c=DbPool.getConnection()){
            c.setAutoCommit(false);
            try {
                long applicationId;
                try(PreparedStatement p=c.prepareStatement("INSERT INTO business_applications (user_id,company_name,contact_name,contact_phone,business_type,governorate,business_area,applicant_note) VALUES(?,?,?,?,?,?,?,?)",Statement.RETURN_GENERATED_KEYS)){
                    p.setLong(1,userId);p.setString(2,input.companyName());p.setString(3,input.contactName());
                    p.setString(4,input.phone());p.setString(5,input.businessType());p.setString(6,input.governorate());
                    p.setString(7,input.area());p.setString(8,input.applicantNote());p.executeUpdate();
                    try(ResultSet keys=p.getGeneratedKeys()){if(!keys.next())throw new SQLException("Missing application identifier");applicationId=keys.getLong(1);}
                }
                try(PreparedStatement p=c.prepareStatement("INSERT INTO business_application_events (application_id,actor_user_id,event_type,from_status,to_status) VALUES(?,?,'SUBMITTED',NULL,'PENDING')")){
                    p.setLong(1,applicationId);p.setLong(2,userId);p.executeUpdate();
                }
                c.commit();return applicationId;
            }catch(SQLException|RuntimeException failure){c.rollback();throw failure;}
            finally{c.setAutoCommit(true);}
        }
    }
    public Counts counts() throws SQLException {
        try(Connection c=DbPool.getConnection();PreparedStatement p=c.prepareStatement(
                "SELECT COALESCE(SUM(status='PENDING'),0),COALESCE(SUM(status='APPROVED'),0),COALESCE(SUM(status='REJECTED'),0) FROM business_applications")){
            try(ResultSet r=p.executeQuery()){r.next();return new Counts(r.getLong(1),r.getLong(2),r.getLong(3));}
        }
    }
    public Page list(int page,String filter) throws SQLException {
        String status=TradeRules.listState(filter);
        boolean specific=!"ALL".equals(status);
        String where=specific?" WHERE a.status=?":"";
        try(Connection c=DbPool.getConnection()){
            long total;
            try(PreparedStatement p=c.prepareStatement("SELECT COUNT(*) FROM business_applications a"+where)){
                if(specific)p.setString(1,status);
                try(ResultSet r=p.executeQuery()){r.next();total=r.getLong(1);}
            }
            List<Application> result=new ArrayList<>();
            String query="SELECT "+FIELDS+JOIN+where+" ORDER BY a.created_at DESC,a.id DESC LIMIT ? OFFSET ?";
            try(PreparedStatement p=c.prepareStatement(query)){
                int i=1;if(specific)p.setString(i++,status);
                p.setInt(i++,TradeRules.PAGE_SIZE);p.setLong(i,(long)(page-1)*TradeRules.PAGE_SIZE);
                try(ResultSet r=p.executeQuery()){while(r.next())result.add(map(r,true));}
            }
            return new Page(List.copyOf(result),total,page,TradeRules.PAGE_SIZE);
        }
    }
    public Application detail(long id) throws SQLException {
        try(Connection c=DbPool.getConnection();PreparedStatement p=c.prepareStatement(
                "SELECT "+FIELDS+JOIN+" WHERE a.id=?")){
            p.setLong(1,id);
            try(ResultSet r=p.executeQuery()){return r.next()?map(r,true):null;}
        }
    }
    /** Only a PENDING application may transition once. Lock guards concurrent reviews. */
    public boolean review(long applicationId,long adminUserId,String decision,String privateNote) throws SQLException {
        String target=TradeRules.reviewState(decision);
        String note=TradeRules.field(privateNote,"Internal review note",0,500,true);
        try(Connection c=DbPool.getConnection()){
            c.setAutoCommit(false);
            try {
                String status=null;
                try(PreparedStatement p=c.prepareStatement("SELECT status FROM business_applications WHERE id=? FOR UPDATE")){
                    p.setLong(1,applicationId);
                    try(ResultSet r=p.executeQuery()){if(r.next())status=r.getString(1);}
                }
                if(status==null){c.rollback();return false;}
                if(!"PENDING".equals(status))throw new IllegalStateException("Application already reviewed");
                try(PreparedStatement p=c.prepareStatement("UPDATE business_applications SET status=?,reviewed_by=?,reviewed_at=CURRENT_TIMESTAMP,admin_note=? WHERE id=? AND status='PENDING'")){
                    p.setString(1,target);p.setLong(2,adminUserId);p.setString(3,note);p.setLong(4,applicationId);
                    if(p.executeUpdate()!=1)throw new IllegalStateException("Concurrent application update");
                }
                try(PreparedStatement p=c.prepareStatement("INSERT INTO business_application_events (application_id,actor_user_id,event_type,from_status,to_status) VALUES(?,?,?,'PENDING',?)")){
                    p.setLong(1,applicationId);p.setLong(2,adminUserId);p.setString(3,target);p.setString(4,target);p.executeUpdate();
                }
                c.commit();return true;
            }catch(SQLException|RuntimeException failure){c.rollback();throw failure;}
            finally{c.setAutoCommit(true);}
        }
    }
    private static Application map(ResultSet r,boolean admin) throws SQLException {
        Timestamp reviewed=r.getTimestamp("reviewed_at");
        return new Application(r.getLong("id"),r.getLong("user_id"),r.getString("company_name"),
            r.getString("contact_name"),r.getString("contact_phone"),r.getString("business_type"),
            r.getString("governorate"),r.getString("business_area"),r.getString("applicant_note"),
            r.getString("status"),r.getString("created_at"),r.getString("updated_at"),
            admin?r.getString("applicant_email"):null,
            reviewed==null?null:reviewed.toString(),admin?r.getString("admin_note"):null);
    }
}

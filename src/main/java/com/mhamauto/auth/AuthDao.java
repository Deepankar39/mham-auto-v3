package com.mhamauto.auth;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Set;

/** All identity persistence uses prepared SQL. Registration always grants CUSTOMER only. */
public final class AuthDao {
    public record Account(AuthPrincipal principal,String passwordHash,boolean active) { }
    private static final String SELECT_USER="SELECT u.id,u.display_name,u.email_normalized,u.password_hash,u.status, r.role_code "
            +"FROM users u LEFT JOIN user_roles ur ON ur.user_id=u.id "
            +"LEFT JOIN roles r ON r.id=ur.role_id ";

    public Account byEmail(Connection c,String email) throws SQLException {
        return query(c,SELECT_USER+" WHERE u.email_normalized=?",email);
    }
    public Account byId(Connection c,long id) throws SQLException {
        try (PreparedStatement ps=c.prepareStatement(SELECT_USER+" WHERE u.id=?")) {
            ps.setLong(1,id);
            return read(ps);
        }
    }
    private Account query(Connection c,String sql,String email) throws SQLException {
        try(PreparedStatement ps=c.prepareStatement(sql)) {ps.setString(1,email);return read(ps);}
    }
    private Account read(PreparedStatement ps) throws SQLException {
        try(ResultSet rs=ps.executeQuery()) {
            long id=0;String name=null,email=null,passwordHash=null,status=null;
            Set<String> roles=new HashSet<>();
            while(rs.next()) {
                if (id==0) {
                    id=rs.getLong("id");name=rs.getString("display_name");email=rs.getString("email_normalized");
                    passwordHash=rs.getString("password_hash");status=rs.getString("status");
                }
                String role=rs.getString("role_code");
                if (role!=null) roles.add(role);
            }
            return id==0?null:new Account(new AuthPrincipal(id,name,email,roles),passwordHash,"ACTIVE".equals(status));
        }
    }
    public AuthPrincipal createCustomer(Connection c,String email,String name,String passwordHash) throws SQLException {
        boolean oldAutoCommit=c.getAutoCommit();
        c.setAutoCommit(false);
        try {
            long id;
            try(PreparedStatement ps=c.prepareStatement("INSERT INTO users(email_normalized,display_name,password_hash) VALUES (?,?,?)",Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1,email);ps.setString(2,name);ps.setString(3,passwordHash);
                ps.executeUpdate();
                try(ResultSet rs=ps.getGeneratedKeys()) {
                    if(!rs.next()) throw new SQLException("New account ID unavailable");
                    id=rs.getLong(1);
                }
            }
            try(PreparedStatement ps=c.prepareStatement("INSERT INTO user_roles(user_id,role_id) SELECT ?,id FROM roles WHERE role_code='CUSTOMER'")) {
                ps.setLong(1,id);
                if (ps.executeUpdate()!=1) throw new SQLException("CUSTOMER role unavailable; apply V002 migration");
            }
            c.commit();
            return new AuthPrincipal(id,name,email,Set.of("CUSTOMER"));
        } catch(SQLException|RuntimeException e) {c.rollback();throw e;}
        finally {c.setAutoCommit(oldAutoCommit);}
    }
    /** Local command-line operation only; never exposed via Servlet. */
    public boolean grantAdmin(Connection c,String email) throws SQLException {
        try(PreparedStatement ps=c.prepareStatement("INSERT IGNORE INTO user_roles(user_id,role_id) "
                +"SELECT u.id,r.id FROM users u JOIN roles r ON r.role_code='ADMIN' "
                +"WHERE u.email_normalized=? AND u.status='ACTIVE'")) {
            ps.setString(1,email);return ps.executeUpdate()==1;
        }
    }
}

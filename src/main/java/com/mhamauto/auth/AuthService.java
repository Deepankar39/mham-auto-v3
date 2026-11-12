package com.mhamauto.auth;

import com.mhamauto.db.DbPool;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;

public final class AuthService {
    private static final AuthDao DAO=new AuthDao();
    private static final String DUMMY_HASH=PasswordHasher.hash("fake-password-for-timing-only-2026");
    private AuthService() { }
    public static AuthPrincipal register(String name,String email,String password,String confirmation) throws SQLException {
        String cleanName=IdentityRules.displayName(name);
        String cleanEmail=IdentityRules.email(email);
        IdentityRules.password(password);
        if (!password.equals(confirmation)) throw new IllegalArgumentException("Passwords do not match.");
        String hash=PasswordHasher.hash(password);
        try(Connection c=DbPool.getConnection()) {
            try {return DAO.createCustomer(c,cleanEmail,cleanName,hash);}
            catch(SQLIntegrityConstraintViolationException duplicate) {
                if (duplicate.getErrorCode()==1062) throw new IllegalArgumentException("An account with that email already exists.");
                throw duplicate;
            }
        }
    }
    public static AuthPrincipal login(String email,String password) throws SQLException {
        String cleanEmail;
        try {cleanEmail=IdentityRules.email(email);} catch(IllegalArgumentException invalid) {cleanEmail="invalid@example.invalid";}
        if (password==null || password.length()>128) password="";
        try(Connection c=DbPool.getConnection()) {
            AuthDao.Account account=DAO.byEmail(c,cleanEmail);
            boolean verified=PasswordHasher.verify(password,account==null?DUMMY_HASH:account.passwordHash());
            if (!verified || account==null || !account.active()) return null;
            return account.principal();
        }
    }
    public static AuthPrincipal refresh(long id) throws SQLException {
        try(Connection c=DbPool.getConnection()) {
            AuthDao.Account account=DAO.byId(c,id);
            return account==null || !account.active()?null:account.principal();
        }
    }
}

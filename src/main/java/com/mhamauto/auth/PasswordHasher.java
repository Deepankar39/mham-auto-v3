package com.mhamauto.auth;

import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/** Versioned PBKDF2-HMAC-SHA256 hashes: Java built-in, random 16-byte salt, 600k iterations. */
public final class PasswordHasher {
    private static final int ITERATIONS=600_000;
    private static final SecureRandom RNG=new SecureRandom();
    private PasswordHasher() { }
    public static String hash(String password) {
        IdentityRules.password(password);
        byte[] salt=new byte[16]; RNG.nextBytes(salt);
        byte[] digest=derive(password,salt,ITERATIONS);
        return "pbkdf2-sha256$"+ITERATIONS+"$"+Base64.getEncoder().encodeToString(salt)
                +"$"+Base64.getEncoder().encodeToString(digest);
    }
    public static boolean verify(String password,String stored) {
        if (password == null || stored == null || password.length()>128) return false;
        try {
            String[] parts=stored.split("\\$",-1);
            if (parts.length != 4 || !"pbkdf2-sha256".equals(parts[0])) return false;
            int work=Integer.parseInt(parts[1]);
            if (work < ITERATIONS || work > 2_000_000) return false;
            byte[] salt=Base64.getDecoder().decode(parts[2]);
            byte[] expected=Base64.getDecoder().decode(parts[3]);
            if (salt.length != 16 || expected.length != 32) return false;
            return MessageDigest.isEqual(expected,derive(password,salt,work));
        } catch (IllegalArgumentException e) { return false; }
    }
    private static byte[] derive(String password,byte[] salt,int iterations) {
        PBEKeySpec spec=new PBEKeySpec(password.toCharArray(),salt,iterations,256);
        try { return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded(); }
        catch (Exception e) {throw new IllegalStateException("Password hashing unavailable",e);}
        finally {spec.clearPassword();}
    }
}

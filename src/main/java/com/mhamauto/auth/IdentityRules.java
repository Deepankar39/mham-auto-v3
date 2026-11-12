package com.mhamauto.auth;

import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.regex.Pattern;

/** Academic input policy. Never trust browser-side validation. */
public final class IdentityRules {
    private static final Pattern EMAIL = Pattern.compile("^[A-Za-z0-9.!#$%&'*+/=?^_`{|}~-]+@[A-Za-z0-9](?:[A-Za-z0-9-]{0,61}[A-Za-z0-9])?(?:\\.[A-Za-z0-9](?:[A-Za-z0-9-]{0,61}[A-Za-z0-9])?)+$");
    private IdentityRules() { }

    public static String email(String value) {
        if (value == null) throw new IllegalArgumentException("Enter a valid email address.");
        String normalized = value.trim().toLowerCase(Locale.ROOT);
        if (normalized.length() > 254 || !EMAIL.matcher(normalized).matches()) {
            throw new IllegalArgumentException("Enter a valid email address.");
        }
        return normalized;
    }
    public static String displayName(String value) {
        if (value == null) throw new IllegalArgumentException("Name must be between 2 and 100 characters.");
        String cleaned=value.strip();
        int len=cleaned.codePointCount(0,cleaned.length());
        if (len < 2 || len > 100 || cleaned.chars().anyMatch(c -> Character.isISOControl((char)c))) {
            throw new IllegalArgumentException("Name must be between 2 and 100 characters.");
        }
        return cleaned;
    }
    public static void password(String value) {
        if (value == null || value.length() < 12 || value.length() > 128
                || value.getBytes(StandardCharsets.UTF_8).length > 512) {
            throw new IllegalArgumentException("Password must have 12 to 128 characters.");
        }
        for (int i=0;i<value.length();i++) {
            if (Character.isISOControl(value.charAt(i))) throw new IllegalArgumentException("Password contains unsupported control characters.");
        }
    }
}

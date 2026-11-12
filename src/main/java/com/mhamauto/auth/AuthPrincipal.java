package com.mhamauto.auth;

import java.io.Serializable;
import java.util.Set;

/** Only minimal identity goes into HttpSession. No password hash, sensitive DB state or credential. */
public record AuthPrincipal(long id,String displayName,String email,Set<String> roles) implements Serializable {
    public AuthPrincipal { roles=Set.copyOf(roles); }
    // Explicit JavaBean getters for broad JSP EL compatibility, in addition to record accessors.
    public String getDisplayName() { return displayName; }
    public String getEmail() { return email; }
    public boolean isAdmin() { return roles.contains("ADMIN"); }
}

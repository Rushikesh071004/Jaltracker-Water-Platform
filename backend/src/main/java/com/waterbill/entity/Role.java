package com.waterbill.entity;

public enum Role {
    ROLE_SUPERADMIN,
    ROLE_ADMIN,
    ROLE_RESIDENT;

    public static Role fromString(String val) {
        if (val == null) return null;
        String normalized = val.trim().toUpperCase();
        if (!normalized.startsWith("ROLE_")) {
            normalized = "ROLE_" + normalized;
        }
        return Role.valueOf(normalized);
    }
}

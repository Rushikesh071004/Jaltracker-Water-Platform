package com.waterbill.dto;

import com.waterbill.entity.AccountStatus;
import com.waterbill.entity.Role;

public class AuthResponse {

    private String token;
    private String tokenType = "Bearer";
    private Long id;
    private String username;
    private String fullName;
    private String email;
    private Role role;
    private AccountStatus accountStatus;
    private Long apartmentId;
    private String apartmentName;
    private Long householdId;
    private String flatNumber;

    public AuthResponse() {}

    public AuthResponse(String token, Long id, String username, String fullName, String email, Role role, AccountStatus accountStatus,
                        Long apartmentId, String apartmentName, Long householdId, String flatNumber) {
        this.token = token;
        this.tokenType = "Bearer";
        this.id = id;
        this.username = username;
        this.fullName = fullName;
        this.email = email;
        this.role = role;
        this.accountStatus = accountStatus;
        this.apartmentId = apartmentId;
        this.apartmentName = apartmentName;
        this.householdId = householdId;
        this.flatNumber = flatNumber;
    }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public String getTokenType() { return tokenType; }
    public void setTokenType(String tokenType) { this.tokenType = tokenType; }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    public AccountStatus getAccountStatus() { return accountStatus; }
    public void setAccountStatus(AccountStatus accountStatus) { this.accountStatus = accountStatus; }

    public Long getApartmentId() { return apartmentId; }
    public void setApartmentId(Long apartmentId) { this.apartmentId = apartmentId; }

    public String getApartmentName() { return apartmentName; }
    public void setApartmentName(String apartmentName) { this.apartmentName = apartmentName; }

    public Long getHouseholdId() { return householdId; }
    public void setHouseholdId(Long householdId) { this.householdId = householdId; }

    public String getFlatNumber() { return flatNumber; }
    public void setFlatNumber(String flatNumber) { this.flatNumber = flatNumber; }
}

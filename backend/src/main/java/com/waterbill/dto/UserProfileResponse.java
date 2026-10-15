package com.waterbill.dto;

import com.waterbill.entity.AccountStatus;
import com.waterbill.entity.Role;
import java.time.OffsetDateTime;

public class UserProfileResponse {

    private Long id;
    private String username;
    private String fullName;
    private String email;
    private String phone;
    private Role role;
    private AccountStatus accountStatus;
    private Long apartmentId;
    private String apartmentName;
    private Long householdId;
    private String flatNumber;
    private Boolean isMetered;
    private OffsetDateTime createdAt;

    public UserProfileResponse() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

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

    public Boolean getIsMetered() { return isMetered; }
    public void setIsMetered(Boolean metered) { isMetered = metered; }

    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
}

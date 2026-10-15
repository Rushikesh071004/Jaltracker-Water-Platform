package com.waterbill.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "apartments")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Apartment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Apartment name is required")
    @Column(nullable = false)
    private String name;

    @NotBlank(message = "Address is required")
    @Column(nullable = false, columnDefinition = "TEXT")
    private String address;

    @Column(name = "total_flats", nullable = false)
    private Integer totalFlats = 0;

    @Column(name = "contact_email")
    private String contactEmail;

    @Column(name = "contact_phone")
    private String contactPhone;

    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    @JsonIgnore
    @OneToMany(mappedBy = "apartment", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Household> households = new ArrayList<>();

    @JsonIgnore
    @OneToMany(mappedBy = "apartment", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TariffPlan> tariffPlans = new ArrayList<>();

    @JsonIgnore
    @OneToMany(mappedBy = "apartment", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BillingCycle> billingCycles = new ArrayList<>();

    @JsonIgnore
    @OneToMany(mappedBy = "apartment")
    private List<User> users = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        this.createdAt = OffsetDateTime.now();
        this.updatedAt = OffsetDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = OffsetDateTime.now();
    }

    public Apartment() {}

    public Apartment(String name, String address, Integer totalFlats, String contactEmail, String contactPhone) {
        this.name = name;
        this.address = address;
        this.totalFlats = totalFlats;
        this.contactEmail = contactEmail;
        this.contactPhone = contactPhone;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public Integer getTotalFlats() { return totalFlats; }
    public void setTotalFlats(Integer totalFlats) { this.totalFlats = totalFlats; }

    public String getContactEmail() { return contactEmail; }
    public void setContactEmail(String contactEmail) { this.contactEmail = contactEmail; }

    public String getContactPhone() { return contactPhone; }
    public void setContactPhone(String contactPhone) { this.contactPhone = contactPhone; }

    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }

    public OffsetDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(OffsetDateTime updatedAt) { this.updatedAt = updatedAt; }

    public List<Household> getHouseholds() { return households; }
    public void setHouseholds(List<Household> households) { this.households = households; }

    public List<TariffPlan> getTariffPlans() { return tariffPlans; }
    public void setTariffPlans(List<TariffPlan> tariffPlans) { this.tariffPlans = tariffPlans; }

    public List<BillingCycle> getBillingCycles() { return billingCycles; }
    public void setBillingCycles(List<BillingCycle> billingCycles) { this.billingCycles = billingCycles; }

    public List<User> getUsers() { return users; }
    public void setUsers(List<User> users) { this.users = users; }
}

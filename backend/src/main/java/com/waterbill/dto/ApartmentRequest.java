package com.waterbill.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class ApartmentRequest {

    @NotBlank(message = "Apartment name is required")
    private String name;

    @NotBlank(message = "Address is required")
    private String address;

    @NotNull(message = "Total flats count is required")
    @Positive(message = "Total flats must be at least 1")
    private Integer totalFlats;

    private String contactEmail;
    private String contactPhone;

    public ApartmentRequest() {}

    public ApartmentRequest(String name, String address, Integer totalFlats, String contactEmail, String contactPhone) {
        this.name = name;
        this.address = address;
        this.totalFlats = totalFlats;
        this.contactEmail = contactEmail;
        this.contactPhone = contactPhone;
    }

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
}

package com.waterbill.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotBlank;

public class LoginRequest {

    @JsonAlias({"usernameOrEmail", "identifier"})
    private String username;

    private String email;

    @NotBlank(message = "Password is required")
    private String password;

    public LoginRequest() {}

    public LoginRequest(String usernameOrEmail, String password) {
        this.username = usernameOrEmail;
        this.email = usernameOrEmail;
        this.password = password;
    }

    public String getUsername() {
        return username != null ? username : email;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email != null ? email : username;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getIdentifier() {
        if (username != null && !username.trim().isEmpty()) {
            return username.trim();
        }
        if (email != null && !email.trim().isEmpty()) {
            return email.trim();
        }
        return "";
    }
}

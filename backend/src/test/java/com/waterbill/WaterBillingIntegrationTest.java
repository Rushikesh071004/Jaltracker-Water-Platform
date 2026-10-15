package com.waterbill;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.waterbill.dto.LoginRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class WaterBillingIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Health endpoint is public and returns UP status")
    void testHealthEndpoint() throws Exception {
        mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.service", containsString("Water Consumption")));
    }

    @Test
    @DisplayName("Admin login generates JWT and allows access to protected admin APIs")
    void testAdminAuthenticationAndAccess() throws Exception {
        LoginRequest loginRequest = new LoginRequest("admin@waterbill.com", "Admin@123");

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.role").value("ROLE_ADMIN"))
                .andExpect(jsonPath("$.email").value("admin@waterbill.com"))
                .andReturn();

        String responseBody = loginResult.getResponse().getContentAsString();
        String token = objectMapper.readTree(responseBody).get("token").asText();

        // Access /api/auth/me
        mockMvc.perform(get("/api/auth/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("admin@waterbill.com"))
                .andExpect(jsonPath("$.role").value("ROLE_ADMIN"));

        // Access Admin Apartments endpoint
        mockMvc.perform(get("/api/admin/apartments")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))));
    }

    @Test
    @DisplayName("Resident login allows access to resident profile and comparison analytics")
    void testResidentAuthenticationAndAnalytics() throws Exception {
        LoginRequest loginRequest = new LoginRequest("resident1@waterbill.com", "Resident@123");

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.role").value("ROLE_RESIDENT"))
                .andExpect(jsonPath("$.householdId").value(1))
                .andReturn();

        String token = objectMapper.readTree(loginResult.getResponse().getContentAsString()).get("token").asText();

        // Access comparative analytics
        mockMvc.perform(get("/api/resident/analytics/comparison?householdId=1")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.flatNumber").value("A-101"))
                .andExpect(jsonPath("$.conservationStatus").isNotEmpty())
                .andExpect(jsonPath("$.dailyTrend").isArray());
    }

    @Test
    @DisplayName("PDF Invoice Download generates valid application/pdf payload")
    void testPdfInvoiceDownload() throws Exception {
        LoginRequest loginRequest = new LoginRequest("resident1@waterbill.com", "Resident@123");
        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        String token = objectMapper.readTree(loginResult.getResponse().getContentAsString()).get("token").asText();

        // Download invoice 1
        mockMvc.perform(get("/api/invoices/1/download")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_PDF))
                .andExpect(header().string("Content-Disposition", containsString("attachment; filename=")));
    }
}

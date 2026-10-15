package com.waterbill.controller;

import com.waterbill.dto.AuthResponse;
import com.waterbill.service.OtpService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
public class OtpController {

    private static final Logger log = LoggerFactory.getLogger(OtpController.class);

    private final OtpService otpService;

    public OtpController(OtpService otpService) {
        this.otpService = otpService;
    }

    /**
     * Requirement 3: POST /sendOtp (also mapped to /api/otp/sendOtp and /api/auth/sendOtp)
     * Triggers OtpService to dispatch OTP via both WhatsApp and Email.
     */
    @PostMapping(value = {"/sendOtp", "/api/sendOtp", "/api/otp/sendOtp", "/api/auth/sendOtp"})
    public ResponseEntity<Map<String, Object>> sendOtp(@RequestBody Map<String, String> payload) {
        String identifier = payload.get("identifier");
        if (identifier == null || identifier.isBlank()) {
            identifier = payload.get("username");
        }
        if (identifier == null || identifier.isBlank()) {
            identifier = payload.get("email");
        }
        if (identifier == null || identifier.isBlank()) {
            identifier = payload.get("phoneNumber");
        }

        if (identifier == null || identifier.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Identifier (username, email, or phone) is required to dispatch OTP."
            ));
        }

        String password = payload.get("password");

        log.info("Received request to send OTP via WhatsApp and Email for identifier: {}", identifier);
        Map<String, Object> response = otpService.sendOtp(identifier, password);
        return ResponseEntity.ok(response);
    }

    /**
     * Requirement 3: POST /validateOtp (also mapped to /api/validateOtp, /api/otp/validateOtp and /api/auth/validateOtp)
     * Validates inputted OTP code and finalizes authentication by issuing JWT.
     */
    @PostMapping(value = {"/validateOtp", "/api/validateOtp", "/api/otp/validateOtp", "/api/auth/validateOtp"})
    public ResponseEntity<AuthResponse> validateOtp(@RequestBody Map<String, String> payload) {
        String identifier = payload.get("identifier");
        if (identifier == null || identifier.isBlank()) {
            identifier = payload.get("username");
        }
        if (identifier == null || identifier.isBlank()) {
            identifier = payload.get("email");
        }

        String otp = payload.get("otp");
        if (otp == null || otp.isBlank()) {
            otp = payload.get("otpCode");
        }
        if (otp == null || otp.isBlank()) {
            otp = payload.get("code");
        }

        if (identifier == null || identifier.isBlank() || otp == null || otp.isBlank()) {
            throw new IllegalArgumentException("Both identifier and otp code are required.");
        }

        log.info("Validating OTP for identifier: {}", identifier);
        AuthResponse authResponse = otpService.validateOtp(identifier, otp);
        return ResponseEntity.ok(authResponse);
    }
}

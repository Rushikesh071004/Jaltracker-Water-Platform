package com.waterbill.service;

import com.waterbill.config.JwtTokenProvider;
import com.waterbill.dto.*;
import com.waterbill.entity.AccountStatus;
import com.waterbill.entity.Apartment;
import com.waterbill.entity.EmailOtpToken;
import com.waterbill.entity.Household;
import com.waterbill.entity.Role;
import com.waterbill.entity.User;
import com.waterbill.repository.ApartmentRepository;
import com.waterbill.repository.EmailOtpTokenRepository;
import com.waterbill.repository.HouseholdRepository;
import com.waterbill.repository.UserRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final ApartmentRepository apartmentRepository;
    private final HouseholdRepository householdRepository;
    private final EmailOtpTokenRepository otpTokenRepository;
    private final EmailNotificationService notificationService;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
    private final AuthenticationManager authenticationManager;
    private final NotificationBroadcastService notificationBroadcastService;

    public UserService(UserRepository userRepository,
                       ApartmentRepository apartmentRepository,
                       HouseholdRepository householdRepository,
                       EmailOtpTokenRepository otpTokenRepository,
                       EmailNotificationService notificationService,
                       PasswordEncoder passwordEncoder,
                       JwtTokenProvider tokenProvider,
                       AuthenticationManager authenticationManager,
                       NotificationBroadcastService notificationBroadcastService) {
        this.userRepository = userRepository;
        this.apartmentRepository = apartmentRepository;
        this.householdRepository = householdRepository;
        this.otpTokenRepository = otpTokenRepository;
        this.notificationService = notificationService;
        this.passwordEncoder = passwordEncoder;
        this.tokenProvider = tokenProvider;
        this.authenticationManager = authenticationManager;
        this.notificationBroadcastService = notificationBroadcastService;
    }

    public AuthResponse authenticate(LoginRequest request) {
        String identifier = request.getIdentifier();
        if (identifier.isEmpty()) {
            throw new BadCredentialsException("Username or email is required");
        }

        User user = userRepository.findByEmailOrUsername(identifier.trim().toLowerCase())
                .orElseThrow(() -> new BadCredentialsException("Invalid username or password"));

        if (user.getAccountStatus() == AccountStatus.PENDING) {
            throw new BadCredentialsException("Account pending approval by Super Admin");
        }
        if (user.getAccountStatus() == AccountStatus.REJECTED) {
            throw new BadCredentialsException("Account registration has been rejected by Super Admin");
        }

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(user.getEmail(), request.getPassword())
            );

            String token = tokenProvider.generateToken(user);
            return mapToAuthResponse(user, token);
        } catch (BadCredentialsException ex) {
            throw new BadCredentialsException("Invalid username or password");
        }
    }

    @Transactional
    public Map<String, Object> sendLoginOtp(String email) {
        String cleanEmail = email.trim().toLowerCase();
        User user = userRepository.findByEmail(cleanEmail)
                .orElseThrow(() -> new IllegalArgumentException("No account found with email: " + cleanEmail));

        if (user.getAccountStatus() == AccountStatus.PENDING) {
            throw new IllegalStateException("Your account is pending verification by an administrator.");
        }
        if (user.getAccountStatus() == AccountStatus.REJECTED) {
            throw new IllegalStateException("Your account registration has been rejected.");
        }

        // Generate 6-digit OTP
        String otpCode = String.format("%06d", new Random().nextInt(1000000));
        OffsetDateTime expiresAt = OffsetDateTime.now().plusMinutes(10);

        EmailOtpToken token = new EmailOtpToken(cleanEmail, otpCode, expiresAt);
        otpTokenRepository.save(token);

        // Send via Email / Log
        notificationService.sendOtpEmail(cleanEmail, otpCode);

        Map<String, Object> resp = new HashMap<>();
        resp.put("success", true);
        resp.put("message", "A 6-digit login verification code has been dispatched to " + cleanEmail);
        resp.put("email", cleanEmail);
        resp.put("demoOtp", otpCode); // For convenient local testing & demo
        return resp;
    }

    @Transactional
    public AuthResponse verifyLoginOtp(String email, String otpCode) {
        String cleanEmail = email.trim().toLowerCase();
        String cleanOtp = otpCode.trim();

        EmailOtpToken token = otpTokenRepository.findFirstByEmailIgnoreCaseAndOtpCodeAndUsedFalseAndExpiresAtAfterOrderByCreatedAtDesc(
                cleanEmail, cleanOtp, OffsetDateTime.now()
        ).orElseThrow(() -> new BadCredentialsException("Invalid or expired verification code. Please request a new code."));

        token.setUsed(true);
        otpTokenRepository.save(token);

        User user = userRepository.findByEmail(cleanEmail)
                .orElseThrow(() -> new BadCredentialsException("User not found"));

        if (user.getAccountStatus() == AccountStatus.PENDING) {
            throw new BadCredentialsException("Account pending approval by Super Admin");
        }
        if (user.getAccountStatus() == AccountStatus.REJECTED) {
            throw new BadCredentialsException("Account registration has been rejected by Super Admin");
        }

        String jwtToken = tokenProvider.generateToken(user);
        return mapToAuthResponse(user, jwtToken);
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String cleanEmail = request.getEmail().trim().toLowerCase();
        if (userRepository.existsByEmail(cleanEmail)) {
            throw new IllegalArgumentException("Email is already registered: " + cleanEmail);
        }

        String username = request.getUsername();
        if (username != null && !username.isBlank()) {
            username = username.trim().toLowerCase();
            if (userRepository.existsByUsername(username)) {
                throw new IllegalArgumentException("Username is already taken: " + username);
            }
        } else {
            username = cleanEmail.split("@")[0].replaceAll("[^a-zA-Z0-9._-]", "");
            if (userRepository.existsByUsername(username)) {
                username = username + (System.currentTimeMillis() % 10000);
            }
        }

        Apartment apartment = null;
        if (request.getApartmentId() != null) {
            apartment = apartmentRepository.findById(request.getApartmentId()).orElse(null);
        }
        if (apartment == null) {
            apartment = apartmentRepository.findAll().stream().findFirst().orElse(null);
        }

        Household household = null;
        if (request.getHouseholdId() != null) {
            household = householdRepository.findById(request.getHouseholdId()).orElse(null);
            if (household != null && apartment == null) {
                apartment = household.getApartment();
            }
        }

        final Apartment resolvedApartment = apartment;
        if (household == null && request.getFlatNumber() != null && !request.getFlatNumber().isBlank() && resolvedApartment != null) {
            String flatNumber = request.getFlatNumber().trim().toUpperCase();
            household = householdRepository.findByApartmentIdAndFlatNumber(resolvedApartment.getId(), flatNumber)
                    .orElseGet(() -> {
                        Household newH = new Household(resolvedApartment, flatNumber, new BigDecimal("1000.00"), true);
                        return householdRepository.save(newH);
                    });
        }

        Role assignedRole = request.getRole() != null ? request.getRole() : Role.ROLE_RESIDENT;

        User user = new User();
        user.setFullName(request.getFullName().trim());
        user.setUsername(username);
        user.setEmail(cleanEmail);
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setPhone(request.getPhone());
        user.setRole(assignedRole);
        user.setApartment(apartment);
        user.setHousehold(household);
        user.setIsActive(true);

        // All newly registered accounts default strictly to PENDING status
        user.setAccountStatus(AccountStatus.PENDING);

        User savedUser = userRepository.save(user);
        String token = tokenProvider.generateToken(savedUser);
        return mapToAuthResponse(savedUser, token);
    }

    @Transactional(readOnly = true)
    public List<UserProfileResponse> getPendingUsers(String callerEmail) {
        User caller = userRepository.findByEmail(callerEmail)
                .orElseThrow(() -> new IllegalArgumentException("Caller not found"));

        List<User> pending;
        if (caller.getRole() == Role.ROLE_SUPERADMIN) {
            // Superadmin views all pending accounts across the entire platform
            pending = userRepository.findByAccountStatus(AccountStatus.PENDING);
        } else if (caller.getRole() == Role.ROLE_ADMIN) {
            // Admin only views pending RESIDENT accounts in their apartment
            Long aptId = caller.getApartment() != null ? caller.getApartment().getId() : 1L;
            pending = userRepository.findByApartmentIdAndAccountStatus(aptId, AccountStatus.PENDING)
                    .stream()
                    .filter(u -> u.getRole() == Role.ROLE_RESIDENT)
                    .collect(Collectors.toList());
        } else {
            throw new AccessDeniedException("Access denied to pending accounts.");
        }

        return pending.stream().map(this::mapToProfileResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<UserProfileResponse> getAllUsers(String callerEmail) {
        User caller = userRepository.findByEmail(callerEmail)
                .orElseThrow(() -> new IllegalArgumentException("Caller not found"));

        List<User> list;
        if (caller.getRole() == Role.ROLE_SUPERADMIN) {
            list = userRepository.findAll();
        } else if (caller.getRole() == Role.ROLE_ADMIN) {
            Long aptId = caller.getApartment() != null ? caller.getApartment().getId() : 1L;
            list = userRepository.findByApartmentId(aptId);
        } else {
            throw new AccessDeniedException("Access denied.");
        }

        return list.stream().map(this::mapToProfileResponse).collect(Collectors.toList());
    }

    @Transactional
    public UserProfileResponse approveUser(Long userId, String callerEmail) {
        User caller = userRepository.findByEmail(callerEmail)
                .orElseThrow(() -> new IllegalArgumentException("Caller not found"));
        User target = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Target user not found with ID: " + userId));

        if (caller.getRole() == Role.ROLE_SUPERADMIN) {
            // Superadmin can approve any account
            target.setAccountStatus(AccountStatus.VERIFIED);
        } else if (caller.getRole() == Role.ROLE_ADMIN) {
            if (target.getRole() == Role.ROLE_ADMIN || target.getRole() == Role.ROLE_SUPERADMIN) {
                throw new AccessDeniedException("Only a Superadmin can approve Administrator accounts.");
            }
            if (caller.getApartment() != null && target.getApartment() != null
                    && !caller.getApartment().getId().equals(target.getApartment().getId())) {
                throw new AccessDeniedException("You can only verify residents within your own society.");
            }
            target.setAccountStatus(AccountStatus.VERIFIED);
        } else {
            throw new AccessDeniedException("You do not have permissions to verify accounts.");
        }

        User saved = userRepository.save(target);
        String flatNum = saved.getHousehold() != null ? saved.getHousehold().getFlatNumber() : "N/A";
        notificationBroadcastService.notifyResidentVerification(saved.getId(), saved.getUsername(), saved.getFullName(), flatNum, true);
        return mapToProfileResponse(saved);
    }

    @Transactional
    public UserProfileResponse rejectUser(Long userId, String callerEmail) {
        User caller = userRepository.findByEmail(callerEmail)
                .orElseThrow(() -> new IllegalArgumentException("Caller not found"));
        User target = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Target user not found with ID: " + userId));

        if (caller.getRole() == Role.ROLE_SUPERADMIN) {
            target.setAccountStatus(AccountStatus.REJECTED);
        } else if (caller.getRole() == Role.ROLE_ADMIN) {
            if (target.getRole() == Role.ROLE_ADMIN || target.getRole() == Role.ROLE_SUPERADMIN) {
                throw new AccessDeniedException("Only a Superadmin can manage Administrator accounts.");
            }
            target.setAccountStatus(AccountStatus.REJECTED);
        } else {
            throw new AccessDeniedException("You do not have permissions to reject accounts.");
        }

        User saved = userRepository.save(target);
        String flatNum = saved.getHousehold() != null ? saved.getHousehold().getFlatNumber() : "N/A";
        notificationBroadcastService.notifyResidentVerification(saved.getId(), saved.getUsername(), saved.getFullName(), flatNum, false);
        return mapToProfileResponse(saved);
    }

    @Transactional(readOnly = true)
    public UserProfileResponse getProfileByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + email));
        return mapToProfileResponse(user);
    }

    @Transactional
    public UserProfileResponse updateProfile(String email, String fullName, String phone) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + email));

        if (fullName != null && !fullName.isBlank()) {
            user.setFullName(fullName.trim());
        }
        if (phone != null) {
            user.setPhone(phone.trim());
        }

        User updated = userRepository.save(user);
        return mapToProfileResponse(updated);
    }

    @Transactional
    public void changePassword(String email, PasswordChangeRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + email));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
            throw new BadCredentialsException("Current password does not match");
        }

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    @Transactional
    public UserProfileResponse assignResidentToHousehold(Long userId, Long householdId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));
        Household household = householdRepository.findById(householdId)
                .orElseThrow(() -> new IllegalArgumentException("Household not found with ID: " + householdId));

        user.setHousehold(household);
        user.setApartment(household.getApartment());
        User updated = userRepository.save(user);
        return mapToProfileResponse(updated);
    }

    private AuthResponse mapToAuthResponse(User user, String token) {
        Long apartmentId = user.getApartment() != null ? user.getApartment().getId() : null;
        String apartmentName = user.getApartment() != null ? user.getApartment().getName() : null;
        Long householdId = user.getHousehold() != null ? user.getHousehold().getId() : null;
        String flatNumber = user.getHousehold() != null ? user.getHousehold().getFlatNumber() : null;

        return new AuthResponse(
                token,
                user.getId(),
                user.getUsername(),
                user.getFullName(),
                user.getEmail(),
                user.getRole(),
                user.getAccountStatus(),
                apartmentId,
                apartmentName,
                householdId,
                flatNumber
        );
    }

    private UserProfileResponse mapToProfileResponse(User user) {
        UserProfileResponse resp = new UserProfileResponse();
        resp.setId(user.getId());
        resp.setUsername(user.getUsername());
        resp.setFullName(user.getFullName());
        resp.setEmail(user.getEmail());
        resp.setPhone(user.getPhone());
        resp.setRole(user.getRole());
        resp.setAccountStatus(user.getAccountStatus());
        resp.setCreatedAt(user.getCreatedAt());

        if (user.getApartment() != null) {
            resp.setApartmentId(user.getApartment().getId());
            resp.setApartmentName(user.getApartment().getName());
        }

        if (user.getHousehold() != null) {
            resp.setHouseholdId(user.getHousehold().getId());
            resp.setFlatNumber(user.getHousehold().getFlatNumber());
            resp.setIsMetered(user.getHousehold().getIsMetered());
        }

        return resp;
    }
}

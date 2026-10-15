package com.waterbill.repository;

import com.waterbill.entity.EmailOtpToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.Optional;

@Repository
public interface EmailOtpTokenRepository extends JpaRepository<EmailOtpToken, Long> {

    Optional<EmailOtpToken> findFirstByEmailIgnoreCaseAndOtpCodeAndUsedFalseAndExpiresAtAfterOrderByCreatedAtDesc(
            String email, String otpCode, OffsetDateTime now);

    void deleteByExpiresAtBefore(OffsetDateTime cutoff);
}

package com.waterbill.repository;

import com.waterbill.entity.AccountStatus;
import com.waterbill.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Optional<User> findByUsername(String username);
    boolean existsByEmail(String email);
    boolean existsByUsername(String username);

    @Query("SELECT u FROM User u WHERE LOWER(u.email) = LOWER(:identifier) OR (u.username IS NOT NULL AND LOWER(u.username) = LOWER(:identifier))")
    Optional<User> findByEmailOrUsername(@Param("identifier") String identifier);

    List<User> findByApartmentId(Long apartmentId);
    List<User> findByHouseholdId(Long householdId);
    List<User> findByAccountStatus(AccountStatus accountStatus);
    List<User> findByApartmentIdAndAccountStatus(Long apartmentId, AccountStatus accountStatus);
}

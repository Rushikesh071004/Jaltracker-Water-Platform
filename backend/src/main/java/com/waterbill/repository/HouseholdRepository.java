package com.waterbill.repository;

import com.waterbill.entity.Household;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HouseholdRepository extends JpaRepository<Household, Long> {
    List<Household> findByApartmentId(Long apartmentId);
    List<Household> findByApartmentIdAndIsMetered(Long apartmentId, Boolean isMetered);
    Optional<Household> findByApartmentIdAndFlatNumber(Long apartmentId, String flatNumber);
}

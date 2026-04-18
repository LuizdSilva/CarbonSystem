package com.carbontreesystem.repository;

import com.carbontreesystem.model.CarbonCredit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface CarbonCreditRepository extends JpaRepository<CarbonCredit, Long> {

    @Query("SELECT COALESCE(SUM(c.creditsCalculated), 0.0) FROM CarbonCredit c WHERE c.validated = true")
    Double sumValidatedCredits();

    @Query("SELECT COALESCE(SUM(c.creditsCalculated), 0.0) FROM CarbonCredit c")
    Double totalCredits();
}
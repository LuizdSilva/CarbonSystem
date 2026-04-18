package com.carbontreesystem.repository;

import com.carbontreesystem.model.ConformityParameter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface ConformityParameterRepository extends JpaRepository<ConformityParameter, Long> {
    Optional<ConformityParameter> findFirstByActiveTrue();
}
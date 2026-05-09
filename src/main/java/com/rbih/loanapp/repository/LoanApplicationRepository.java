package com.rbih.loanapp.repository;

import com.rbih.loanapp.domain.entity.LoanApplicationEntity;
import com.rbih.loanapp.domain.enums.ApplicationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Persistence port for loan application audit records.
 *
 * Extends JpaRepository to inherit save/findById/findAll without boilerplate.
 * Custom finders are declared here as needed; Spring Data generates the
 * implementations at startup from the method names.
 *
 * Only audit-read queries are exposed here — all write operations flow
 * through the service layer, which constructs the entity and calls save().
 */
@Repository
public interface LoanApplicationRepository extends JpaRepository<LoanApplicationEntity, UUID> {

    /**
     * Retrieve all applications for a given outcome — useful for audit dashboards.
     */
    List<LoanApplicationEntity> findByStatus(ApplicationStatus status);
}

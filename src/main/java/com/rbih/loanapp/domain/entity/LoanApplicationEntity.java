package com.rbih.loanapp.domain.entity;

import com.rbih.loanapp.domain.enums.ApplicationStatus;
import com.rbih.loanapp.domain.enums.RiskBand;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Root audit entity — one row per submitted loan application.
 *
 * Design notes:
 * - UUID primary key avoids sequential-ID enumeration attacks.
 * - Applicant, loan, and offer data are @Embedded so a single SELECT
 *   fetches the complete audit record without joins.
 * - rejectionReasons stored as an @ElementCollection (separate table)
 *   because the list is only needed on rejection paths and keeps the
 *   main entity table clean.
 */
@Entity
@Table(name = "loan_applications")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoanApplicationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ApplicationStatus status;

    @Enumerated(EnumType.STRING)
    private RiskBand riskBand;

    @Embedded
    private ApplicantDetails applicant;

    @Embedded
    private LoanDetails loan;

    @Embedded
    private OfferDetails offer;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "loan_rejection_reasons",
            joinColumns = @JoinColumn(name = "application_id")
    )
    @Column(name = "reason")
    private List<String> rejectionReasons;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    private void prePersist() {
        createdAt = Instant.now();
    }
}

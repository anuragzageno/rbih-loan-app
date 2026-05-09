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
    @AttributeOverrides({
            @AttributeOverride(name = "name",          column = @Column(name = "applicant_name")),
            @AttributeOverride(name = "age",           column = @Column(name = "applicant_age")),
            @AttributeOverride(name = "monthlyIncome", column = @Column(name = "applicant_monthly_income", precision = 15, scale = 2)),
            @AttributeOverride(name = "employmentType",column = @Column(name = "applicant_employment_type")),
            @AttributeOverride(name = "creditScore",   column = @Column(name = "applicant_credit_score"))
    })
    private ApplicantDetails applicant;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "amount",       column = @Column(name = "loan_amount", precision = 15, scale = 2)),
            @AttributeOverride(name = "tenureMonths", column = @Column(name = "loan_tenure_months")),
            @AttributeOverride(name = "purpose",      column = @Column(name = "loan_purpose"))
    })
    private LoanDetails loan;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "interestRate",  column = @Column(name = "offer_interest_rate", precision = 6, scale = 2)),
            @AttributeOverride(name = "tenureMonths", column = @Column(name = "offer_tenure_months")),
            @AttributeOverride(name = "emi",           column = @Column(name = "offer_emi", precision = 15, scale = 2)),
            @AttributeOverride(name = "totalPayable",  column = @Column(name = "offer_total_payable", precision = 15, scale = 2))
    })
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

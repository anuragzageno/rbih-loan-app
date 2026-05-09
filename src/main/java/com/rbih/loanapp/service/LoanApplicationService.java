package com.rbih.loanapp.service;

import com.rbih.loanapp.domain.entity.ApplicantDetails;
import com.rbih.loanapp.domain.entity.LoanApplicationEntity;
import com.rbih.loanapp.domain.entity.LoanDetails;
import com.rbih.loanapp.domain.entity.OfferDetails;
import com.rbih.loanapp.domain.enums.ApplicationStatus;
import com.rbih.loanapp.domain.enums.RiskBand;
import com.rbih.loanapp.dto.request.ApplicantRequest;
import com.rbih.loanapp.dto.request.LoanApplicationRequest;
import com.rbih.loanapp.dto.request.LoanRequest;
import com.rbih.loanapp.dto.response.LoanApplicationResponse;
import com.rbih.loanapp.dto.response.OfferResponse;
import com.rbih.loanapp.repository.LoanApplicationRepository;
import com.rbih.loanapp.service.calculation.EmiCalculator;
import com.rbih.loanapp.service.calculation.InterestRateCalculator;
import com.rbih.loanapp.service.calculation.RiskBandClassifier;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * Primary orchestrating service for loan application evaluation.
 *
 * Evaluation pipeline:
 *  1. If credit score >= 600: classify risk band, derive interest rate, compute EMI.
 *  2. Run all eligibility rules (credit score, age+tenure, EMI 60% cap).
 *  3. If any rule fails → REJECTED (all reasons reported).
 *  4. If EMI > 50% of income → REJECTED (offer not viable).
 *  5. Otherwise → APPROVED with generated offer.
 *  6. Persist the full audit record and return the response DTO.
 *
 * The two EMI thresholds serve different purposes:
 *  - 60% = hard eligibility boundary (reported as rejection reason)
 *  - 50% = offer viability boundary (offer cannot be generated above this)
 */
@Service
@RequiredArgsConstructor
@Transactional
public class LoanApplicationService {

    private static final BigDecimal OFFER_EMI_INCOME_LIMIT = new BigDecimal("0.50");

    private final RiskBandClassifier riskBandClassifier;
    private final InterestRateCalculator interestRateCalculator;
    private final EmiCalculator emiCalculator;
    private final EligibilityEvaluator eligibilityEvaluator;
    private final LoanApplicationRepository repository;

    public LoanApplicationResponse evaluate(LoanApplicationRequest request) {
        ApplicantRequest applicant = request.getApplicant();
        LoanRequest loan = request.getLoan();

        // Step 1 — Calculate risk band, rate and EMI only if credit score qualifies.
        // These are needed for the EMI-based eligibility check (rule 3).
        RiskBand riskBand = null;
        BigDecimal interestRate = null;
        BigDecimal emi = null;

        if (applicant.getCreditScore() >= 600) {
            riskBand = riskBandClassifier.classify(applicant.getCreditScore());
            interestRate = interestRateCalculator.calculate(
                    riskBand, applicant.getEmploymentType(), loan.getAmount());
            emi = emiCalculator.calculateEmi(loan.getAmount(), interestRate, loan.getTenureMonths());
        }

        // Step 2 — Evaluate eligibility rules; collect all failures.
        List<String> rejectionReasons = eligibilityEvaluator.evaluate(
                applicant.getCreditScore(), applicant.getAge(),
                loan.getTenureMonths(), applicant.getMonthlyIncome(), emi);

        // Step 3 — Hard eligibility failure.
        if (!rejectionReasons.isEmpty()) {
            return persistAndBuildResponse(request, ApplicationStatus.REJECTED, riskBand,
                    null, null, null, rejectionReasons);
        }

        // Step 4 — Offer viability check (EMI must be ≤ 50% of income).
        BigDecimal offerEmiCap = applicant.getMonthlyIncome().multiply(OFFER_EMI_INCOME_LIMIT);
        if (emi.compareTo(offerEmiCap) > 0) {
            return persistAndBuildResponse(request, ApplicationStatus.REJECTED, riskBand,
                    null, null, null, List.of("EMI_EXCEEDS_50_PERCENT"));
        }

        // Step 5 — Approved: generate offer.
        BigDecimal totalPayable = emiCalculator.calculateTotalPayable(emi, loan.getTenureMonths());
        return persistAndBuildResponse(request, ApplicationStatus.APPROVED, riskBand,
                interestRate, emi, totalPayable, null);
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private LoanApplicationResponse persistAndBuildResponse(
            LoanApplicationRequest request,
            ApplicationStatus status,
            RiskBand riskBand,
            BigDecimal interestRate,
            BigDecimal emi,
            BigDecimal totalPayable,
            List<String> rejectionReasons) {

        OfferDetails offerDetails = (status == ApplicationStatus.APPROVED)
                ? OfferDetails.builder()
                        .interestRate(interestRate)
                        .tenureMonths(request.getLoan().getTenureMonths())
                        .emi(emi)
                        .totalPayable(totalPayable)
                        .build()
                : null;

        LoanApplicationEntity entity = LoanApplicationEntity.builder()
                .status(status)
                .riskBand(riskBand)
                .applicant(toApplicantDetails(request.getApplicant()))
                .loan(toLoanDetails(request.getLoan()))
                .offer(offerDetails)
                .rejectionReasons(rejectionReasons)
                .build();

        LoanApplicationEntity saved = repository.save(entity);

        OfferResponse offerResponse = (status == ApplicationStatus.APPROVED)
                ? OfferResponse.builder()
                        .interestRate(interestRate)
                        .tenureMonths(request.getLoan().getTenureMonths())
                        .emi(emi)
                        .totalPayable(totalPayable)
                        .build()
                : null;

        return LoanApplicationResponse.builder()
                .applicationId(saved.getId())
                .status(status)
                .riskBand(riskBand)
                .offer(offerResponse)
                .rejectionReasons(rejectionReasons)
                .build();
    }

    private ApplicantDetails toApplicantDetails(ApplicantRequest req) {
        return ApplicantDetails.builder()
                .name(req.getName())
                .age(req.getAge())
                .monthlyIncome(req.getMonthlyIncome())
                .employmentType(req.getEmploymentType())
                .creditScore(req.getCreditScore())
                .build();
    }

    private LoanDetails toLoanDetails(LoanRequest req) {
        return LoanDetails.builder()
                .amount(req.getAmount())
                .tenureMonths(req.getTenureMonths())
                .purpose(req.getPurpose())
                .build();
    }
}

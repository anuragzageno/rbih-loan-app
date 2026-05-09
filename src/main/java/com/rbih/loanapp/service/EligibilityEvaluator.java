package com.rbih.loanapp.service;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Evaluates the three eligibility rules and collects all failing reasons.
 *
 * Rules (per spec):
 *  1. Credit score must be >= 600  → CREDIT_SCORE_BELOW_MINIMUM
 *  2. age + floor(tenureMonths / 12) must not exceed 65 → AGE_TENURE_LIMIT_EXCEEDED
 *  3. EMI must not exceed 60% of monthly income → EMI_EXCEEDS_60_PERCENT
 *     (only evaluated when emi is non-null, i.e. credit score was sufficient
 *      to reach the calculation step)
 *
 * All failing rules are collected so the response can report every rejection
 * reason rather than short-circuiting at the first failure.
 */
@Component
public class EligibilityEvaluator {

    private static final int MIN_CREDIT_SCORE = 600;
    private static final int MAX_AGE_AT_LOAN_END = 65;
    private static final BigDecimal EMI_TO_INCOME_HARD_LIMIT = new BigDecimal("0.60");

    /**
     * @param creditScore     applicant credit score
     * @param age             applicant age in years
     * @param tenureMonths    requested loan tenure in months
     * @param monthlyIncome   applicant monthly income
     * @param emi             calculated monthly EMI, or {@code null} if credit
     *                        score was too low to reach the calculation step
     * @return list of rejection reason codes; empty if all rules pass
     */
    public List<String> evaluate(int creditScore, int age, int tenureMonths,
                                 BigDecimal monthlyIncome, BigDecimal emi) {
        List<String> reasons = new ArrayList<>();

        if (creditScore < MIN_CREDIT_SCORE) {
            reasons.add("CREDIT_SCORE_BELOW_MINIMUM");
        }

        // Tenure expressed in whole years (floor division) — conservative and
        // straightforward for the age-at-end-of-loan boundary check.
        int tenureYears = tenureMonths / 12;
        if (age + tenureYears > MAX_AGE_AT_LOAN_END) {
            reasons.add("AGE_TENURE_LIMIT_EXCEEDED");
        }

        if (emi != null) {
            BigDecimal hardLimit = monthlyIncome.multiply(EMI_TO_INCOME_HARD_LIMIT);
            if (emi.compareTo(hardLimit) > 0) {
                reasons.add("EMI_EXCEEDS_60_PERCENT");
            }
        }

        return reasons;
    }
}

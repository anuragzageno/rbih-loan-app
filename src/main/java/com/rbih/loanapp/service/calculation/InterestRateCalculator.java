package com.rbih.loanapp.service.calculation;

import com.rbih.loanapp.domain.enums.EmploymentType;
import com.rbih.loanapp.domain.enums.RiskBand;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Derives the final annual interest rate for a loan offer.
 *
 * Formula (per spec):
 *   Final Rate = Base Rate + Risk Premium + Employment Premium + Loan Size Premium
 *
 * Base rate      : 12.00%
 * Risk premium   : LOW → 0%, MEDIUM → 1.5%, HIGH → 3%
 * Employment     : SALARIED → 0%, SELF_EMPLOYED → 1%
 * Loan size      : amount > 10,00,000 → 0.5%, otherwise 0%
 *
 * All rates are expressed as percentages (e.g. 12.00 means 12%).
 * No rounding is applied here — the EMI calculator works with higher precision
 * internally and rounds only the final monetary outputs.
 */
@Component
public class InterestRateCalculator {

    private static final BigDecimal BASE_RATE              = new BigDecimal("12.00");
    private static final BigDecimal RISK_PREMIUM_MEDIUM    = new BigDecimal("1.5");
    private static final BigDecimal RISK_PREMIUM_HIGH      = new BigDecimal("3.0");
    private static final BigDecimal EMPLOYMENT_PREMIUM     = new BigDecimal("1.0");
    private static final BigDecimal LOAN_SIZE_PREMIUM      = new BigDecimal("0.5");
    private static final BigDecimal LOAN_SIZE_THRESHOLD    = new BigDecimal("1000000");

    public BigDecimal calculate(RiskBand riskBand, EmploymentType employmentType, BigDecimal loanAmount) {
        BigDecimal rate = BASE_RATE
                .add(riskPremium(riskBand))
                .add(employmentPremium(employmentType))
                .add(loanSizePremium(loanAmount));
        return rate;
    }

    private BigDecimal riskPremium(RiskBand riskBand) {
        return switch (riskBand) {
            case LOW    -> BigDecimal.ZERO;
            case MEDIUM -> RISK_PREMIUM_MEDIUM;
            case HIGH   -> RISK_PREMIUM_HIGH;
        };
    }

    private BigDecimal employmentPremium(EmploymentType employmentType) {
        return employmentType == EmploymentType.SELF_EMPLOYED ? EMPLOYMENT_PREMIUM : BigDecimal.ZERO;
    }

    private BigDecimal loanSizePremium(BigDecimal loanAmount) {
        return loanAmount.compareTo(LOAN_SIZE_THRESHOLD) > 0 ? LOAN_SIZE_PREMIUM : BigDecimal.ZERO;
    }
}

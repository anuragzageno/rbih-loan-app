package com.rbih.loanapp.service.calculation;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

/**
 * Calculates EMI and total payable using the standard reducing-balance formula.
 *
 * Formula:
 *   EMI = P × r × (1+r)^n  /  ((1+r)^n − 1)
 *
 * Where:
 *   P = principal (loan amount)
 *   r = monthly interest rate = annualRatePercent / 1200
 *   n = tenure in months
 *
 * Precision strategy:
 *   - Monthly rate and intermediate power are computed at scale=10 / MathContext(20)
 *     to avoid accumulated rounding error across potentially 360 iterations of ^n.
 *   - Final EMI and totalPayable are rounded to scale=2, HALF_UP per spec.
 */
@Component
public class EmiCalculator {

    private static final int    INTERMEDIATE_SCALE = 10;
    private static final int    MONEY_SCALE        = 2;
    private static final RoundingMode ROUNDING     = RoundingMode.HALF_UP;

    /**
     * @param principal        loan amount (P)
     * @param annualRatePercent annual interest rate as a percentage, e.g. 13.5 for 13.5%
     * @param tenureMonths     number of monthly instalments (n)
     * @return calculated EMI rounded to 2 decimal places
     */
    public BigDecimal calculateEmi(BigDecimal principal, BigDecimal annualRatePercent, int tenureMonths) {
        // r = annual% / 1200
        BigDecimal monthlyRate = annualRatePercent.divide(
                BigDecimal.valueOf(1200), INTERMEDIATE_SCALE, ROUNDING);

        // (1 + r)^n  — BigDecimal.pow() handles integer exponents exactly
        BigDecimal onePlusR = BigDecimal.ONE.add(monthlyRate);
        BigDecimal onePlusRPowN = onePlusR.pow(tenureMonths, new MathContext(20, ROUNDING));

        // Numerator: P * r * (1+r)^n
        BigDecimal numerator = principal
                .multiply(monthlyRate)
                .multiply(onePlusRPowN);

        // Denominator: (1+r)^n - 1
        BigDecimal denominator = onePlusRPowN.subtract(BigDecimal.ONE);

        return numerator.divide(denominator, MONEY_SCALE, ROUNDING);
    }

    /**
     * @param emi          monthly EMI
     * @param tenureMonths number of instalments
     * @return total amount payable over the loan lifetime, rounded to 2 dp
     */
    public BigDecimal calculateTotalPayable(BigDecimal emi, int tenureMonths) {
        return emi.multiply(BigDecimal.valueOf(tenureMonths))
                  .setScale(MONEY_SCALE, ROUNDING);
    }
}

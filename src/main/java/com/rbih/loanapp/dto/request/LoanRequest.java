package com.rbih.loanapp.dto.request;

import com.rbih.loanapp.domain.enums.LoanPurpose;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Loan details portion of the loan application request.
 *
 * Validation bounds per spec:
 *  - amount      : 10,000 – 50,00,000
 *  - tenureMonths: 6–360
 */
@Getter
@NoArgsConstructor
public class LoanRequest {

    @NotNull(message = "Loan amount is required")
    @DecimalMin(value = "10000.00", message = "Loan amount must be at least ₹10,000")
    @DecimalMax(value = "5000000.00", message = "Loan amount must not exceed ₹50,00,000")
    @Digits(integer = 13, fraction = 2, message = "Loan amount has an invalid format")
    private BigDecimal amount;

    @Min(value = 6, message = "Tenure must be at least 6 months")
    @Max(value = 360, message = "Tenure must not exceed 360 months")
    private int tenureMonths;

    @NotNull(message = "Loan purpose is required")
    private LoanPurpose purpose;
}

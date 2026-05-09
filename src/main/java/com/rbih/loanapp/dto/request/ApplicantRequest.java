package com.rbih.loanapp.dto.request;

import com.rbih.loanapp.domain.enums.EmploymentType;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Applicant portion of the loan application request.
 *
 * Validation bounds per spec:
 *  - age        : 21–60
 *  - creditScore: 300–900
 *  - monthly income: > 0
 */
@Getter
@NoArgsConstructor
public class ApplicantRequest {

    @NotBlank(message = "Applicant name must not be blank")
    private String name;

    @Min(value = 21, message = "Applicant age must be at least 21")
    @Max(value = 60, message = "Applicant age must not exceed 60")
    private int age;

    @NotNull(message = "Monthly income is required")
    @DecimalMin(value = "0.01", message = "Monthly income must be greater than 0")
    @Digits(integer = 13, fraction = 2, message = "Monthly income has an invalid format")
    private BigDecimal monthlyIncome;

    @NotNull(message = "Employment type is required")
    private EmploymentType employmentType;

    @Min(value = 300, message = "Credit score must be at least 300")
    @Max(value = 900, message = "Credit score must not exceed 900")
    private int creditScore;
}

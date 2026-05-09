package com.rbih.loanapp.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Top-level request body for POST /applications.
 *
 * Both nested objects are validated recursively via @Valid.
 */
@Getter
@NoArgsConstructor
public class LoanApplicationRequest {

    @NotNull(message = "Applicant details are required")
    @Valid
    private ApplicantRequest applicant;

    @NotNull(message = "Loan details are required")
    @Valid
    private LoanRequest loan;
}

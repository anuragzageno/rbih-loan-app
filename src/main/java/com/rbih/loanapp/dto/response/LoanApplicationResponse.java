package com.rbih.loanapp.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.rbih.loanapp.domain.enums.ApplicationStatus;
import com.rbih.loanapp.domain.enums.RiskBand;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.UUID;

/**
 * Top-level response for POST /applications.
 *
 * A single DTO covers both outcomes:
 *  - APPROVED: riskBand + offer are populated; rejectionReasons is null/absent.
 *  - REJECTED: rejectionReasons is populated; offer is null/absent.
 *
 * @JsonInclude(NON_NULL) ensures null fields are omitted from the JSON body,
 * matching the spec's separate approved/rejected shapes without two separate classes.
 */
@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LoanApplicationResponse {

    private UUID applicationId;
    private ApplicationStatus status;
    private RiskBand riskBand;
    private OfferResponse offer;
    private List<String> rejectionReasons;
}

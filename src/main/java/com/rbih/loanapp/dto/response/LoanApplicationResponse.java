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
 * A single DTO covers both APPROVED and REJECTED outcomes:
 *  - APPROVED : riskBand is set, offer is populated, rejectionReasons absent.
 *  - REJECTED : riskBand may be null (explicit null per spec), offer absent,
 *               rejectionReasons populated.
 *
 * Field-level @JsonInclude(NON_NULL) is used on offer and rejectionReasons so
 * they are omitted when not applicable, while riskBand is always serialized
 * (including as JSON null) to match the spec's rejected shape exactly.
 */
@Getter
@Builder
public class LoanApplicationResponse {

    private UUID applicationId;
    private ApplicationStatus status;

    // Always present in the response body — null on REJECTED paths where
    // credit score disqualified before risk classification.
    private RiskBand riskBand;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private OfferResponse offer;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<String> rejectionReasons;
}

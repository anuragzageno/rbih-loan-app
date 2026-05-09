package com.rbih.loanapp.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

/**
 * Offer detail returned inside an APPROVED response.
 */
@Getter
@Builder
public class OfferResponse {

    private BigDecimal interestRate;
    private int tenureMonths;
    private BigDecimal emi;
    private BigDecimal totalPayable;
}

package com.rbih.loanapp.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Embeddable snapshot of the generated offer — null columns when REJECTED.
 */
@Embeddable
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OfferDetails {

    @Column(precision = 6, scale = 2)
    private BigDecimal interestRate;

    private Integer tenureMonths;

    @Column(precision = 15, scale = 2)
    private BigDecimal emi;

    @Column(precision = 15, scale = 2)
    private BigDecimal totalPayable;
}

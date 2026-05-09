package com.rbih.loanapp.service.calculation;

import com.rbih.loanapp.domain.enums.RiskBand;
import org.springframework.stereotype.Component;

/**
 * Classifies an applicant's credit score into a risk band.
 *
 * Bands per spec:
 *   750+      → LOW
 *   650–749   → MEDIUM
 *   600–649   → HIGH
 *   < 600     → not classified (eligibility check rejects before this point)
 *
 * Kept as a standalone @Component so it can be unit-tested in isolation
 * without loading the full Spring context.
 */
@Component
public class RiskBandClassifier {

    public RiskBand classify(int creditScore) {
        if (creditScore >= 750) {
            return RiskBand.LOW;
        } else if (creditScore >= 650) {
            return RiskBand.MEDIUM;
        } else {
            return RiskBand.HIGH;
        }
    }
}

package com.rbih.loanapp.controller;

import com.rbih.loanapp.dto.request.LoanApplicationRequest;
import com.rbih.loanapp.dto.response.LoanApplicationResponse;
import com.rbih.loanapp.service.LoanApplicationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for loan application submission.
 *
 * Intentionally thin — all business logic lives in {@link LoanApplicationService}.
 * The controller is responsible only for HTTP concerns:
 *  - Routing
 *  - Triggering Bean Validation via @Valid
 *  - Setting the HTTP response status
 */
@RestController
@RequestMapping("/applications")
@RequiredArgsConstructor
public class LoanApplicationController {

    private final LoanApplicationService loanApplicationService;

    /**
     * Submit a loan application for evaluation.
     *
     * @param request validated request body
     * @return 201 Created with evaluation result (APPROVED or REJECTED)
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public LoanApplicationResponse submitApplication(@RequestBody @Valid LoanApplicationRequest request) {
        return loanApplicationService.evaluate(request);
    }
}

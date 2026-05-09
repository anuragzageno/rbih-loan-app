package com.rbih.loanapp.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.List;

/**
 * Unified error response envelope for all HTTP 4xx/5xx replies.
 *
 * Shape:
 * {
 *   "timestamp": "2026-05-09T10:15:30Z",
 *   "status": 400,
 *   "error": "Bad Request",
 *   "message": "Validation failed",
 *   "fieldErrors": [              // present only on validation failures
 *     { "field": "applicant.age", "message": "must be between 21 and 60" }
 *   ]
 * }
 */
@Getter
@Builder
public class ErrorResponse {

    private Instant timestamp;
    private int status;
    private String error;
    private String message;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<FieldError> fieldErrors;

    /**
     * Per-field validation error. Modelled as a record (Java 17) — immutable
     * value object with no need for Lombok annotations.
     */
    public record FieldError(String field, String message) {}
}

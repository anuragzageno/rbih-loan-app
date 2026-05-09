package com.rbih.loanapp.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
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
 *
 * Uses @AllArgsConstructor rather than @Builder to avoid a known Lombok
 * conflict when a nested record shares a field name with the outer class.
 */
@Getter
@AllArgsConstructor
public class ErrorResponse {

    private Instant timestamp;
    private int status;
    private String error;
    private String errorMessage;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<FieldError> fieldErrors;

    /**
     * Per-field validation error — Java 17 record, inherently immutable.
     */
    public record FieldError(String field, String message) {}
}

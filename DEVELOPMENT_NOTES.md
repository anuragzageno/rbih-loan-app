# Development Notes — RBIH Loan Application Service

## Overall Approach

Built as a single-responsibility Spring Boot 3 REST service whose sole job is to receive a loan application request, evaluate it against business rules, persist the decision, and return a structured response. The design favours explicitness over magic: every business rule is expressed in its own named component so the evaluation pipeline reads like a checklist.

---

## Key Design Decisions

### 1. Single-table persistence with `@Embeddable`
Applicant details, loan parameters, and offer details are modelled as `@Embeddable` value objects embedded into a single `loan_applications` table with explicit `@AttributeOverrides` on every column. This keeps the audit record self-contained (one row = one decision), avoids joins on read, and makes the H2 schema trivial to inspect.

### 2. UUID primary keys
`GenerationType.UUID` is used for the entity PK. UUIDs prevent sequential ID enumeration attacks and are safe to expose in API responses without leaking record counts.

### 3. Two distinct EMI income thresholds
The business rules define two separate gates:
- **60 % (eligibility gate)** — evaluated by `EligibilityEvaluator`. If EMI exceeds 60 % of monthly income the application is ineligible.
- **50 % (offer viability gate)** — checked in `LoanApplicationService`. Even if technically eligible, an offer is only issued when EMI is within 50 % of income.

This two-tier structure means a future product team can tune the offer gate independently of the hard eligibility cutoff.

### 4. All eligibility rules evaluated before short-circuiting
`EligibilityEvaluator` collects *all* failing rule messages rather than returning on the first failure. Applicants receive a complete list of rejection reasons in a single round-trip, avoiding the frustrating UX of fix-one-error-at-a-time feedback loops.

### 5. BigDecimal precision strategy
- Intermediate values (monthly interest rate, `(1+r)^n`) are computed at `scale=10` with `MathContext(20, HALF_UP)` to avoid accumulated rounding error during the EMI formula.
- Final monetary outputs (EMI, total payable, interest rate) are rounded to `scale=2 HALF_UP` before persistence and API response.

### 6. Response shape — field-level `@JsonInclude`
`@JsonInclude(NON_NULL)` is applied at the field level on `offer` and `rejectionReasons` rather than at the class level. This gives precise control: `riskBand` deliberately serialises as an explicit `null` on rejected responses (per spec), while the offer object is omitted entirely when absent.

### 7. `FieldError` as a Java record
The nested `FieldError` type inside `ErrorResponse` is a Java 17 record. Records provide compact, immutable value-object semantics without Lombok, and avoid the annotation-processor conflict that arises when a Lombok `@Builder` outer class contains an inner `@Builder` class.

---

## Trade-offs Considered

| Decision | Alternative | Reason for choice |
|---|---|---|
| Embedded single table | Separate `applicant`, `loan`, `offer` tables with FK relations | Simpler schema, atomic write, no join overhead for this read pattern |
| H2 in-memory | PostgreSQL/MySQL via Docker | Eliminates external dependency; perfectly adequate for the audit scope of this service |
| All-rules evaluation | Short-circuit on first failure | Better UX; minimal performance difference for 3 rules |
| `BigDecimal` throughout | `double` | Financial calculations require exact decimal arithmetic |
| Single response DTO for both outcomes | Separate `ApprovedResponse` / `RejectedResponse` | Simpler controller contract; field-level nullability achieves the same result |

---

## Assumptions Made

1. **Monetary amounts are in Indian Rupees (₹)** — no currency conversion or multi-currency support is required.
2. **Tenure is fixed** — the offer returns the applicant's requested tenure; the service does not suggest an alternative tenure if the EMI fails the 50 % gate.
3. **Credit score is self-reported** — no external bureau lookup; the submitted score is trusted as-is.
4. **Single applicant per application** — joint or co-applicant scenarios are out of scope.
5. **`ddl-auto=create-drop`** — schema is recreated on each restart; this is intentional for a stateless demo service. A production deployment would use Flyway or Liquibase migrations.
6. **No authentication** — the `/applications` endpoint is open. Authentication and authorisation are assumed to be handled by an API gateway layer.

---

## What I Would Improve With More Time

1. **Integration tests** — `@SpringBootTest` slice tests (or TestContainers with a real DB) to validate the full HTTP→DB→response cycle. Unit tests for each calculator and the evaluator are the immediate priority.
2. **Flyway migrations** — replace `ddl-auto=create-drop` with versioned SQL migrations for safe production deployments.
3. **Pagination for audit queries** — `GET /applications` with cursor or page-based pagination so the audit log doesn't grow into an unbounded response.
4. **Spring Security** — JWT or API-key authentication on the endpoint; role separation between applicant submission and admin audit reads.
5. **Structured logging** — MDC-based correlation IDs on every request/response log line to simplify distributed tracing.
6. **Rate limiting** — per-IP or per-client throttle to prevent submission abuse.
7. **Alternative tenure suggestion** — if the requested tenure fails the 50 % EMI gate, compute and return the minimum tenure at which the application would be approved.
8. **Configurable business rules** — move thresholds (60 %, 50 %, age limit 65, credit floor 600) into `application.properties` so they can be tuned without a code change.

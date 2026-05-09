# rbih-loan-app

A Spring Boot REST service that evaluates loan applications and determines whether a loan offer can be approved based on applicant details and business eligibility rules.

## Tech Stack

- **Java 17**
- **Spring Boot 3.3.5**
- **Spring Data JPA** with H2 in-memory database
- **Spring Validation** (JSR-380 / Bean Validation)
- **Lombok**

---

## Prerequisites

| Tool | Version |
|------|---------|
| JDK  | 17 |
| Maven | 3.8+ |

Make sure `JAVA_HOME` points to JDK 17. On macOS with the bundled JDK:

```bash
export JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-17.jdk/Contents/Home
```

Add this line to your `~/.zshrc` (or `~/.bashrc`) to make it permanent.

---

## Running the Application

```bash
export JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-17.jdk/Contents/Home
mvn spring-boot:run
```

The service starts on **http://localhost:8080**.

---

## API Overview

| Method | Endpoint        | Status on success | Description                        |
|--------|-----------------|-------------------|------------------------------------|
| POST   | `/applications` | `201 Created`     | Submit and evaluate a loan request |

### Request body

```json
{
  "applicant": {
    "name": "Priya Sharma",
    "age": 30,
    "monthlyIncome": 80000,
    "employmentType": "SALARIED",
    "creditScore": 780
  },
  "loan": {
    "amount": 200000,
    "tenureMonths": 24,
    "purpose": "PERSONAL"
  }
}
```

**Field constraints**

| Field | Allowed values |
|---|---|
| `employmentType` | `SALARIED`, `SELF_EMPLOYED` |
| `purpose` | `PERSONAL`, `HOME`, `AUTO` |
| `age` | 21 – 60 |
| `creditScore` | 300 – 900 |
| `amount` | 10 000 – 50 00 000 (₹) |
| `tenureMonths` | 6 – 360 |
| `monthlyIncome` | > 0 |

### Response — Approved

```json
{
  "applicationId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
  "status": "APPROVED",
  "riskBand": "LOW",
  "offer": {
    "interestRate": 12.00,
    "tenureMonths": 24,
    "emi": 9415.70,
    "totalPayable": 225976.80
  }
}
```

### Response — Rejected

```json
{
  "applicationId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
  "status": "REJECTED",
  "riskBand": null,
  "rejectionReasons": [
    "CREDIT_SCORE_BELOW_MINIMUM"
  ]
}
```

**Possible rejection reasons**

| Code | Rule |
|---|---|
| `CREDIT_SCORE_BELOW_MINIMUM` | Credit score < 600 |
| `AGE_TENURE_LIMIT_EXCEEDED` | Age + tenure years > 65 |
| `EMI_EXCEEDS_60_PERCENT` | EMI > 60% of monthly income |
| `EMI_EXCEEDS_50_PERCENT` | EMI > 50% of monthly income (offer not viable) |

---

## Testing with Postman

### Import the collection

1. Open Postman.
2. Click **Import** (top-left).
3. Select `rbih-loan-app.postman_collection.json` from the project root.
4. The collection **RBIH Loan Application Service** appears in your sidebar.

### Set the base URL (optional)

The collection uses a `{{baseUrl}}` variable pre-set to `http://localhost:8080`.  
To change it: open the collection → **Variables** tab → edit `baseUrl`.

### Run all scenarios

**Option A — Run manually**

Expand the collection folders and click **Send** on each request. Each request includes Postman test scripts; results appear in the **Test Results** tab of the response panel.

**Option B — Collection Runner**

1. Right-click the collection → **Run collection**.
2. Leave all requests selected and click **Run RBIH Loan Application Service**.
3. Postman runs every request in order and shows a pass/fail summary.

### Collection structure

| Folder | Requests | What is tested |
|---|---|---|
| Happy Path | 4 | APPROVED decisions across LOW / MEDIUM / HIGH risk bands and employment types |
| Rejection — Business Rules | 5 | Each eligibility rule individually + multiple simultaneous failures |
| Validation Errors — 400 | 5 | Bean Validation failures (missing fields, out-of-range values, bad enum, malformed JSON) |

---

## Inspecting the database

The H2 web console is available while the application is running:

1. Open **http://localhost:8080/h2-console**
2. Use these connection details:
   - **JDBC URL**: `jdbc:h2:mem:loandb`
   - **Username**: `sa`
   - **Password**: *(leave blank)*
3. Click **Connect**

Useful queries:

```sql
-- All decisions
SELECT * FROM loan_applications;

-- Rejection reasons
SELECT * FROM loan_rejection_reasons;

-- Full picture (join)
SELECT la.*, r.rejection_reasons
FROM loan_applications la
LEFT JOIN loan_rejection_reasons r ON la.id = r.loan_application_entity_id;
```

> Data is reset on every restart (`ddl-auto=create-drop`). Submit Postman requests first, then query.

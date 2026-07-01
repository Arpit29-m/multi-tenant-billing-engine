# Billing Engine

A multi-tenant SaaS billing backend where independent businesses can manage customers, invoices, and payments on shared infrastructure — completely isolated from each other.

Built this to understand how platforms like Zoho Books actually work at the backend level. The problems that genuinely took time to get right: preventing double charges when clients retry failed requests, keeping financial arithmetic exact across tax calculations, and making sure tenant data isolation holds even when someone guesses another tenant's invoice ID.


## Tech Stack

| | |
|---|---|
| **Backend** | Java 17, Spring Boot 3 |
| **Database** | PostgreSQL |
| **Auth** | Spring Security + JWT |
| **Docs** | Swagger / OpenAPI 3 |
| **Testing** | JUnit 5, Mockito |
| **DevOps** | Docker, Docker Compose |
| **Cloud** | AWS EC2 + RDS |

---

## Core Problems This Solves

**Monetary precision** — `double` can't represent `0.1` exactly in binary. On a ₹99.99 × 18% GST calculation that gives you `₹17.998199999`. Multiply that across thousands of invoices and you have a real accounting problem. Every monetary field uses `BigDecimal` with explicit rounding.

**Atomic payment recording** — when a payment comes in, three things need to happen: debit the customer balance, credit the tenant account, create the payment record. If the server crashes after step two, you're left with corrupted financial state. `@Transactional` ensures all three succeed together or none of them do.

**Duplicate charges on retry** — a payment request times out, the client retries, and without protection you've charged the customer twice. Each request carries a client-generated idempotency key stored in a deduplication table. If the same key arrives again, the original response is returned without re-processing.

**Concurrent balance corruption** — two simultaneous payment requests on the same account both read `balance = ₹1000`, both subtract `₹800`, both write back `₹200`. But `₹1600` was taken. `@Version` on account entities detects this mid-flight conflict and throws `OptimisticLockException`, which the service layer catches and retries cleanly.

**Tenant data isolation** — every database query is automatically scoped to the `tenant_id` extracted from the JWT. You physically cannot access another tenant's invoices by guessing an ID — the query returns nothing because the tenant context won't match.

---

## API Overview

Full docs at `/swagger-ui.html`. Main endpoints:

```
POST   /api/auth/register          → onboard a new tenant
POST   /api/auth/login             → get JWT token

POST   /api/customers              → add customer
GET    /api/customers              → list (tenant-scoped automatically)

POST   /api/invoices               → create with line items + tax
GET    /api/invoices?status=OVERDUE → filter by status
PUT    /api/invoices/{id}/cancel   → cancel unpaid invoice

POST   /api/payments               → record payment (idempotency key required)
GET    /api/invoices/overdue       → aging buckets: 30 / 60 / 90 days
```

---

## Running Locally

```bash
git clone https://github.com/your-username/billing-engine.git
cd billing-engine

# Start PostgreSQL
docker compose up -d

# Set environment variables
export DB_URL=jdbc:postgresql://localhost:5432/billingdb
export DB_USERNAME=postgres
export DB_PASSWORD=postgres
export JWT_SECRET=your-secret-key

# Run
./mvnw spring-boot:run
```

---

## Tests

```bash
./mvnw test
```

The one worth looking at specifically is `PaymentConcurrencyTest` — it fires 20 threads simultaneously at the same account and asserts the final balance is mathematically exact, confirming the optimistic locking logic actually catches and retries conflicts under real concurrent load.

---

## A Few Design Decisions Worth Mentioning

**Optimistic over pessimistic locking** — pessimistic locking holds a DB row lock for the entire transaction. Most payments in a billing system target different accounts, so conflicts are rare. Optimistic locking adds zero overhead on the happy path and only pays a cost when a conflict actually happens.

**Spring Scheduler over Kafka for reminders** — the overdue reminder pipeline runs once a day across a few thousand invoices at most. A Kafka broker, consumer group, and offset management for that would be infrastructure complexity solving a problem a 10-line scheduled method already handles. Kafka makes sense when volume genuinely demands it — this isn't that case.

---

## What I'd Build Next

Razorpay / Stripe webhook integration for real payment gateway events, PDF invoice generation stored on S3, and per-tenant API rate limiting so one high-traffic tenant doesn't degrade the experience for others.

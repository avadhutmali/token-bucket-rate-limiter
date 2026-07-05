# Token Bucket Rate Limiter Service

A standalone, self-built rate limiting service in Java 21 + Spring Boot — implementing
the mechanics libraries usually hide: concurrency-safe token bucket and sliding window
algorithms, per-client configuration, persistent state, and proven correctness under load.

## Why this exists

Most applications import a rate-limiting library and call it done. This project builds
the algorithm, the concurrency control, and the state management from scratch, to
understand — and be able to defend — every design decision underneath.

## Features

- **Two selectable algorithms per client** — Token Bucket and Sliding Window Counter,
  both implementing a shared `RateLimitAlgorithm` interface, swappable at runtime via
  an admin endpoint with zero code duplication
- **Concurrency-safe** — `ReentrantLock`-guarded state mutation on both algorithms;
  proven with 200 concurrent threads against a single client with zero double-spend
- **Persistent state** — bucket state survives service restarts (file-based H2 for
  local dev, Postgres-ready), with a periodic background flush and immediate writes
  on admin config changes
- **Per-client admin configuration** — runtime-adjustable limits with proportional
  capacity carry-over, so changing a client's limit doesn't unfairly reset or
  penalize their current usage
- **Factory pattern for algorithm construction** — adding a third algorithm requires
  touching only the factory, not the service layer
- **Standard rate-limit response headers** — `X-RateLimit-Limit`, `X-RateLimit-Remaining`,
  `X-RateLimit-Reset` on every response, with `429 Too Many Requests` on deny
- **Load tested with JMeter**:
    - 200 concurrent threads against a single client (50-request limit) →
      exactly 50 allowed, 150 correctly denied, 0% real errors
    - 500 threads × 5 requests across unique clients → **~2,495 requests/second
      sustained, 0% error rate**

## API

### `GET /check?clientId={id}`
Returns `ALLOW` or `DENY` for the given client based on their configured algorithm.

**Response:**
```json
{
  "decision": "ALLOW",
  "limit": 10,
  "remaining": 7,
  "resetAfterSeconds": 0
}
```

### `POST /admin/config`
Configure or reconfigure a client's rate limit.

**Request body:**
```json
{
  "clientId": "abc123",
  "type": "SLIDING_WINDOW",
  "requestLimit": 20,
  "windowSizeSeconds": 2,
  "maxTokens": 0,
  "refileRatePerSecond": 0
}
```

## Design decisions worth noting

- **Sliding window uses the counter approximation, not a request log** — O(1) memory
  per client instead of unbounded growth with request volume
- **Proportional carry-over on config change** — a client at 30% remaining capacity
  stays at 30% remaining after a limit change, rather than resetting to full or
  losing their current standing entirely
- **Periodic flush (5s) for token consumption, immediate write for config changes** —
  losing a few seconds of consumption state on crash is an acceptable trade-off;
  losing an explicit admin decision is not
- **`tryLock()` instead of blocking locks** — under contention, a request fails fast
  with DENY rather than queuing, which is the correct behavior for a rate limiter
  under genuine overload

## Stack

Java 21 · Spring Boot 3.5 · Spring Data JPA · H2 (file-based) · JMeter · Docker · AWS EC2

## Status

🚧 Core feature-complete and load-tested. Docker + cloud deployment in progress.
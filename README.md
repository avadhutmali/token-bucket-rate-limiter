# Token Bucket Rate Limiter Service

A standalone rate limiting service built from scratch in Java 21 + Spring Boot.
Implements the token bucket algorithm with per-client configurable limits,
persistent state, and sliding window mode as an alternative strategy.

## What this is

Most applications import a rate limiting library. This project builds the thing
underneath — the algorithm, the concurrency control, and the distributed state
management — as a first-principles implementation.

## Core features

- Token bucket algorithm with per-client configurable capacity and refill rate
- Sliding window mode selectable per client
- Concurrent request safety — no double-spend under high load
- Bucket state survives service restart (PostgreSQL persistence)
- Standard rate-limit response headers on every request
- Admin endpoint for runtime client configuration
- Load tested at 500+ concurrent requests per second

## Stack

Java 21 · Spring Boot 3.5 · PostgreSQL · Docker · AWS EC2

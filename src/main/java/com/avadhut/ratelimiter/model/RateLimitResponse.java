package com.avadhut.ratelimiter.model;

public class RateLimitResponse {
    private String decisision;
    private long limit;
    private long remaining;
    private long resetAfterSeconds;

    public RateLimitResponse(String decisision,long limit,long remaining,long resetAfterSeconds){
        this.decisision = decisision;
        this.limit = limit;
        this.remaining = remaining;
        this.resetAfterSeconds = resetAfterSeconds;
    }

    public String getDecisision() {
        return decisision;
    }

    public long getLimit() {
        return limit;
    }

    public long getRemaining() {
        return remaining;
    }

    public long getResetAfterSeconds() {
        return resetAfterSeconds;
    }
}

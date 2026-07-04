package com.avadhut.ratelimiter.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "bucket_state")
public class BucketState {

    @Id
    private String clientId;
    private long currentTokens;
    private long lastRefillTime;
    private long maxTokens;
    private long refillRatePerSecond;

    public BucketState() {
    }

    public BucketState(String clientId, long currentTokens, long lastRefillTime, long maxTokens, long refillRatePerSecond) {
        this.clientId = clientId;
        this.currentTokens = currentTokens;
        this.lastRefillTime = lastRefillTime;
        this.maxTokens = maxTokens;
        this.refillRatePerSecond = refillRatePerSecond;
    }

    public String getClientId() {
        return clientId;
    }

    public long getCurrentTokens() {
        return currentTokens;
    }

    public long getLastRefillTime() {
        return lastRefillTime;
    }

    public long getMaxTokens() {
        return maxTokens;
    }

    public long getRefillRatePerSecond() {
        return refillRatePerSecond;
    }
}

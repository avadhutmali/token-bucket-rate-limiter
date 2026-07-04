package com.avadhut.ratelimiter.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "bucket_state")
public class BucketState {

    @Id
    private String clientId;
    private String algorithmType;
    private long maxLimit;
    private long refillRatePerSecond;
    private long windowSizeSeconds;
    private long remainingCapacity;

    public BucketState() {
    }

    public BucketState(String clientId, String algorithmType, long maxLimit, long refillRatePerSecond, long windowSizeSeconds, long remainingCapacity) {
        this.clientId = clientId;
        this.algorithmType = algorithmType;
        this.maxLimit = maxLimit;
        this.refillRatePerSecond = refillRatePerSecond;
        this.windowSizeSeconds = windowSizeSeconds;
        this.remainingCapacity = remainingCapacity;
    }

    public String getClientId() {
        return clientId;
    }

    public String getAlgorithmType() {
        return algorithmType;
    }

    public long getLimit() {
        return maxLimit;
    }

    public long getRefillRatePerSecond() {
        return refillRatePerSecond;
    }

    public long getWindowSizeSeconds() {
        return windowSizeSeconds;
    }

    public long getRemainingCapacity() {
        return remainingCapacity;
    }
}

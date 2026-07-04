package com.avadhut.ratelimiter.model;

public class ClientConfigRequest {
    private String clientId;
    private long maxTokens;
    private long refileRatePerSecond;

    public String getClientId() {
        return clientId;
    }

    public long getMaxTokens() {
        return maxTokens;
    }

    public long getRefileRatePerSecond() {
        return refileRatePerSecond;
    }
}

package com.avadhut.ratelimiter.model;

import com.avadhut.ratelimiter.entity.AlgorithmType;

public class ClientConfigRequest {
    private String clientId;
    private long maxTokens;
    private long refileRatePerSecond;
    private long requestLimit;
    private long windowSizeSeconds;
    private AlgorithmType type;

    public ClientConfigRequest() {
    }

    public ClientConfigRequest(String clientId, long maxTokens, long refileRatePerSecond,
                               long requestLimit, long windowSizeSeconds, AlgorithmType type) {
        this.clientId = clientId;
        this.maxTokens = maxTokens;
        this.refileRatePerSecond = refileRatePerSecond;
        this.requestLimit = requestLimit;
        this.windowSizeSeconds = windowSizeSeconds;
        this.type = type;
    }

    public long getRequestLimit() { return requestLimit; }
    public long getWindowSizeSeconds() { return windowSizeSeconds; }
    public AlgorithmType getType() { return type; }
    public String getClientId() { return clientId; }
    public long getMaxTokens() { return maxTokens; }
    public long getRefileRatePerSecond() { return refileRatePerSecond; }
}
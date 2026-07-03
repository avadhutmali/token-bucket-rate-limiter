package com.avadhut.ratelimiter.core;


public class TokenBucket {
    private long maxTokens;
    private long currentTokens;
    private long refillRatePerSecond;
    private long lastRefilTime;

    public TokenBucket(long maxTokens, long refillRatePerSecond){
        this.maxTokens = maxTokens;
        this.currentTokens = maxTokens;
        this.refillRatePerSecond = refillRatePerSecond;
        this.lastRefilTime = System.nanoTime();
    }
    public boolean tryConsume(){
        long currentTime = System.nanoTime();
        long timeElapsed = currentTime - lastRefilTime;

        lastRefilTime = currentTime;

        long tokensToAdd = (timeElapsed/1_000_000_000L) * refillRatePerSecond;

        currentTokens = Math.min(currentTokens + tokensToAdd, maxTokens);

        if(currentTokens>=1){
            currentTokens--;
            return true;
        }
        return false;
    }
}

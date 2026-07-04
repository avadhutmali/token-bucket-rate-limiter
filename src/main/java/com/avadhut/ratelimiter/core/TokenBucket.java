package com.avadhut.ratelimiter.core;


import jakarta.annotation.PostConstruct;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

public class TokenBucket implements RateLimitAlgorithm{
    private long maxTokens;
    private long currentTokens;
    private long refillRatePerSecond;
    private long lastRefilTime;

    public TokenBucket(long maxTokens, long refillRatePerSecond, long currentTokens, long lastRefilTime){
        this.maxTokens = maxTokens;
        this.currentTokens = currentTokens;
        this.refillRatePerSecond = refillRatePerSecond;
        this.lastRefilTime = lastRefilTime;
    }

    public TokenBucket(long maxTokens,long refillRatePerSecond){
        this.maxTokens = maxTokens;
        this.currentTokens = maxTokens;
        this.refillRatePerSecond = refillRatePerSecond;
        this.lastRefilTime = System.nanoTime();
    }

    public long getRefillRatePerSecond() {
        return refillRatePerSecond;
    }

    public long getMaxTokens() {
        return maxTokens;
    }

    public long getLastRefilTime() {
        return lastRefilTime;
    }

    public ReentrantLock getLock() {
        return lock;
    }


    private final ReentrantLock lock = new ReentrantLock();

    public boolean tryConsume(){
        try {
            if(!lock.tryLock(50, TimeUnit.MILLISECONDS)){
                return false;
            }
        }
        catch (InterruptedException e){
            Thread.currentThread().interrupt();
            return false;
        }

        try {
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
        } finally {
            lock.unlock();
        }

    }
    public long getCurrentTokens() {
        return currentTokens;
    }
}

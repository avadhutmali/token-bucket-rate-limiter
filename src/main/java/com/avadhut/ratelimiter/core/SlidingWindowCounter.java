package com.avadhut.ratelimiter.core;

import com.avadhut.ratelimiter.entity.AlgorithmType;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

public class SlidingWindowCounter implements RateLimitAlgorithm {

    private long requestLimit;
    private long windowSizeSeconds;
    private long currentWindowStart;
    private long currentWindowCount;
    private long previousWindowCount;

    //const - 1 for starting new fresh
    public SlidingWindowCounter(long requestLimit, long windowSizeSeconds){
        this.requestLimit = requestLimit;
        this.windowSizeSeconds = windowSizeSeconds;
        this.currentWindowStart = System.nanoTime();
        this.currentWindowCount = 0;
        this.previousWindowCount = 0;
    }

    //const -2 for restoration from the db
    public SlidingWindowCounter(long requestLimit, long windowSizeSeconds, long currentWindowCount, long previousWindowCount, long currentWindowStart){
        this.requestLimit = requestLimit;
        this.windowSizeSeconds = windowSizeSeconds;
        this.currentWindowStart = currentWindowStart;
        this.currentWindowCount = currentWindowCount;
        this.previousWindowCount = previousWindowCount;
    }

    //const -3 for configuraion update
    public SlidingWindowCounter(long newRequestLimit, long newWindowSizeSeconds,long alreadyConsumed){
        this.requestLimit = newRequestLimit;
        this.windowSizeSeconds = newWindowSizeSeconds;
        this.currentWindowStart = System.nanoTime();
        this.currentWindowCount = alreadyConsumed;
        this.previousWindowCount = 0;
    }

    private ReentrantLock lock = new ReentrantLock();

    public boolean tryConsume() {
        try{
            if((!lock.tryLock(50, TimeUnit.MILLISECONDS))){
                return false;
            }
        }catch (InterruptedException e){
            Thread.currentThread().interrupt();
            return false;
        }

        try {
            long currentTime = System.nanoTime();
            long timeElasped = (currentTime - currentWindowStart)/(1_000_000_000);
            long windowsElapsed = timeElasped / windowSizeSeconds;

            if(windowsElapsed >= 2){
                previousWindowCount = 0;
                currentWindowCount = 0;
                currentWindowStart = currentTime;
                timeElasped = 0;
            }
            else if(windowsElapsed == 1){
                previousWindowCount = currentWindowCount;
                currentWindowCount = 0;
                currentWindowStart = currentTime;
                timeElasped = 0;
            }

            double fraction = (double) timeElasped/windowSizeSeconds;
            long totalRequest  = (long)(currentWindowCount + (1-fraction)*previousWindowCount);

            if(totalRequest < requestLimit){
                currentWindowCount ++;
                return true;
            }
            return false;
        }
        finally {
            lock.unlock();
        }
    }

    public long getRemainingCapacity() {
        long currentTime = System.nanoTime();
        long timeElapsed = (currentTime - currentWindowStart) / 1_000_000_000;
        double fraction = (double) timeElapsed / windowSizeSeconds;
        long weightedCount = (long)(currentWindowCount + (1 - fraction) * previousWindowCount);
        return Math.max(0, requestLimit - weightedCount);
    }

    public long getLimit(){
        return requestLimit;
    }

    public AlgorithmType getAlgorithmType() { return AlgorithmType.SLIDING_WINDOW; }

}

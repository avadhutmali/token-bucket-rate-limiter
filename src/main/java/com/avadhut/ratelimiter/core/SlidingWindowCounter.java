package com.avadhut.ratelimiter.core;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

public class SlidingWindowCounter implements RateLimitAlgorithm {

    private long requestLimit;
    private long windowSizeSeconds;
    private long currentWindowStart;
    private long currentWindowCount;
    private long previousWindowCount;

    public SlidingWindowCounter(long requestLimit, long windowSizeSeconds){
        this.requestLimit = requestLimit;
        this.windowSizeSeconds = windowSizeSeconds;
        this.currentWindowStart = System.nanoTime();
        this.currentWindowCount = 0;
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
}

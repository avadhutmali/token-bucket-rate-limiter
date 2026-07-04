package com.avadhut.ratelimiter.core;

import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

public class TokenBucketTest {

    @Test
    public void shouldAllowWhenTokensAvailable(){
        TokenBucket tokenBucket = new TokenBucket(5,0);

        for(int i=0;i<5;i++){
            assertTrue(tokenBucket.tryConsume(),"Request " + i + " should be allowed");
        }
    }

    @Test
    public void shouldDenyWhenBucketEmpty(){
        TokenBucket tokenBucket = new TokenBucket(0,0);
        assertFalse(tokenBucket.tryConsume(),"Should not allow for empty ");
    }

    @Test
    public void shouldRefillAfterWaiting() throws InterruptedException {
        TokenBucket tokenBucket = new TokenBucket(1,1);
        tokenBucket.tryConsume();
        Thread.sleep(2000);
        assertTrue(tokenBucket.tryConsume(),"Should be true as 2 sec elasped");
    }

    @Test
    public void shouldHandleConcurrency() throws InterruptedException {
        TokenBucket tokenBucket = new TokenBucket(10,0);
        int threadCounts = 100;
        ExecutorService service = Executors.newFixedThreadPool(threadCounts);
        AtomicInteger allowedCount = new AtomicInteger();

        for(int i=0;i<threadCounts;i++){
            service.submit(()->{
                if(tokenBucket.tryConsume())allowedCount.incrementAndGet();
            });
        }
        service.shutdown();
        service.awaitTermination(5, TimeUnit.SECONDS);

        assertTrue( allowedCount.get()<=10, "More spend Detected "+ allowedCount.get());

    }

    @Test
    public void remainingCapacityShouldDecreaseAsTokensAreConsumed(){
        TokenBucket bucket = new TokenBucket(10, 0);
        assertTrue(bucket.tryConsume());
        assertEquals(9, bucket.getRemainingCapacity(), "After 1 consume, 9 should remain");
        assertTrue(bucket.tryConsume());
        assertEquals(8, bucket.getRemainingCapacity(), "After 2 consumes, 8 should remain");
    }

}

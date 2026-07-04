package com.avadhut.ratelimiter.core;


import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SlidingWindowCounterTest {

    @Test
    public void shouldAllowUpToLimitWithinWindow(){
        RateLimitAlgorithm algo = new SlidingWindowCounter(5,1);
        for(int i=0;i<5;i++){
            assertTrue(algo.tryConsume(), "Request " + i + " should be allowed") ;
        }
    }

    @Test
    public void shouldDenyWhenLimitExceededInWindow(){
        RateLimitAlgorithm algo = new SlidingWindowCounter(5,1);
        for(int i=0;i<5;i++){
            assertTrue(algo.tryConsume(), "Request " + i + " should be allowed") ;
        }
        assertFalse(algo.tryConsume(), "Request " + 6 + " should not be allowed") ;
    }

    @Test
    public void shouldAllowAgainAfterWindowPasses() throws InterruptedException {
        RateLimitAlgorithm algo = new SlidingWindowCounter(2,1);
        algo.tryConsume();
        algo.tryConsume();

        Thread.sleep(2000);
        assertTrue(algo.tryConsume(), "Request should be allowed as time exceeded") ;
    }
}

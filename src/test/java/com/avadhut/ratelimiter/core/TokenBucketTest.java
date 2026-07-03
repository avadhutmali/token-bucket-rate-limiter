package com.avadhut.ratelimiter.core;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

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

}

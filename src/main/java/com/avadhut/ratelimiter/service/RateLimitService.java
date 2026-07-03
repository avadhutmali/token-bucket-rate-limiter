package com.avadhut.ratelimiter.service;

import com.avadhut.ratelimiter.core.TokenBucket;
import com.avadhut.ratelimiter.model.RateLimitResponse;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;

@Service
public class RateLimitService {
    private final ConcurrentHashMap<String, TokenBucket> buckets = new ConcurrentHashMap<>();

    private static final long DEFAULT_CAPACITY = 10;
    private static final long DEFAULT_REFILL_RATE = 5;

    public RateLimitResponse checkLimit(String userId){
        TokenBucket bucket = buckets.computeIfAbsent(userId,id->new TokenBucket(DEFAULT_CAPACITY,DEFAULT_REFILL_RATE));

        boolean allowed = bucket.tryConsume();
        String decision = allowed ? "ALLOW" : "DENY";

        return new RateLimitResponse(
                decision,
                DEFAULT_CAPACITY,
                bucket.getCurrentTokens(),
                0L
        );
    }
}

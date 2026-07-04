package com.avadhut.ratelimiter.service;

import com.avadhut.ratelimiter.core.TokenBucket;
import com.avadhut.ratelimiter.entity.BucketState;
import com.avadhut.ratelimiter.model.RateLimitResponse;
import com.avadhut.ratelimiter.repository.RateLimiterRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;

@Service
public class RateLimitService {
    private final ConcurrentHashMap<String, TokenBucket> buckets = new ConcurrentHashMap<>();
    private final RateLimiterRepository repository;

    public RateLimitService(RateLimiterRepository repository){
        this.repository = repository;
    }

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

    @PostConstruct
    public void loadFromDatabase(){
        repository.findAll().forEach(state ->
                buckets.put(state.getClientId(), new TokenBucket(
                        state.getMaxTokens(),
                        state.getRefillRatePerSecond(),
                        state.getCurrentTokens(),
                        state.getLastRefillTime()
                ))
        );
        System.out.println("Loaded "+ buckets.size() +" buckets from DB");
    }

    @Scheduled(fixedDelay = 5000)
    public void flushToDataBase(){
        buckets.forEach((clientId,bucket)->
            repository.save(
                    new BucketState(
                        clientId,
                        bucket.getCurrentTokens(),
                        bucket.getLastRefilTime(),
                        bucket.getMaxTokens(),
                        bucket.getRefillRatePerSecond()
                    )
            )
        );

        System.out.println("Flushed "+buckets.size()+" buckets to DB");
    }
}

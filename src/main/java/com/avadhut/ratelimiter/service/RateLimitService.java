package com.avadhut.ratelimiter.service;

import com.avadhut.ratelimiter.core.TokenBucket;
import com.avadhut.ratelimiter.entity.BucketState;
import com.avadhut.ratelimiter.model.RateLimitResponse;
import com.avadhut.ratelimiter.repository.RateLimiterRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Optional;
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

    //important method for the client
    public RateLimitResponse checkLimit(String userId){
        TokenBucket bucket = buckets.computeIfAbsent(userId,id->new TokenBucket(DEFAULT_CAPACITY,DEFAULT_REFILL_RATE));

        boolean allowed = bucket.tryConsume();
        String decision = allowed ? "ALLOW" : "DENY";

        return new RateLimitResponse(
                decision,
                bucket.getMaxTokens(),
                bucket.getCurrentTokens(),
                0L
        );
    }

    //update the config for the per client
    public void updateConfigRequest(String clientId, long newMaxTokens, long newRefilRate ){
        Optional<BucketState> currBucket = repository.findById(clientId);

        if(currBucket.isEmpty()){
            TokenBucket bucket = new TokenBucket(newMaxTokens,newRefilRate);
            buckets.put(clientId,bucket);
            repository.save(
                    new BucketState(
                            clientId,
                            newMaxTokens,
                            System.nanoTime(),
                            newMaxTokens,
                            newRefilRate
                    )
            );
        }
        else{
            TokenBucket liveBucket = buckets.get(clientId);
            long currStateTokens = liveBucket != null ? liveBucket.getCurrentTokens() : currBucket.get().getCurrentTokens();
            long currMaxTokens = currBucket.get().getMaxTokens();

            double fillRatio = (double) currStateTokens / currMaxTokens;
            long newCurrentTokens = (long) (fillRatio * newMaxTokens);

            TokenBucket bucket = new TokenBucket(newMaxTokens,newRefilRate,newCurrentTokens,System.nanoTime());
            buckets.put(clientId,bucket);
            repository.save(
                    new BucketState(
                            clientId,
                            newCurrentTokens,
                            buckets.get(clientId).getLastRefilTime(),
                            newMaxTokens,
                            newRefilRate
                    )
            );

        }
    }

    //load before statring the service
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

    //after scheduled time it flush into database
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

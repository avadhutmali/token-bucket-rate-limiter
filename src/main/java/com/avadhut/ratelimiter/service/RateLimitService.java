package com.avadhut.ratelimiter.service;

import com.avadhut.ratelimiter.core.RateLimitAlgorithm;
import com.avadhut.ratelimiter.core.TokenBucket;
import com.avadhut.ratelimiter.core.factory.RateLimitAlgorithmFactory;
import com.avadhut.ratelimiter.entity.AlgorithmType;
import com.avadhut.ratelimiter.entity.BucketState;
import com.avadhut.ratelimiter.model.ClientConfigRequest;
import com.avadhut.ratelimiter.model.RateLimitResponse;
import com.avadhut.ratelimiter.repository.RateLimiterRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RateLimitService {
    private final ConcurrentHashMap<String, RateLimitAlgorithm> buckets = new ConcurrentHashMap<>();
    private final RateLimiterRepository repository;

    public RateLimitService(RateLimiterRepository repository){
        this.repository = repository;
    }

    private static final long DEFAULT_CAPACITY = 10;
    private static final long DEFAULT_REFILL_RATE = 5;

    //important method for the client
    public RateLimitResponse checkLimit(String userId){
        RateLimitAlgorithm bucket = buckets.computeIfAbsent(userId,id->new TokenBucket(DEFAULT_CAPACITY,DEFAULT_REFILL_RATE));

        boolean allowed = bucket.tryConsume();
        String decision = allowed ? "ALLOW" : "DENY";

        return new RateLimitResponse(
                decision,
                bucket.getLimit(),
                bucket.getRemainingCapacity(),
                0L
        );
    }

    //update the config for the per client
    public void updateConfigRequest(String clientId, AlgorithmType type, ClientConfigRequest request ){

        RateLimitAlgorithm oldAlgorithm = buckets.get(clientId);
        RateLimitAlgorithm newAlgorithm;

        if(oldAlgorithm == null){
            newAlgorithm = RateLimitAlgorithmFactory.create(type,request);
        }else{
            double remainingRatio = (double) oldAlgorithm.getRemainingCapacity()/oldAlgorithm.getLimit();
            long newLimit = RateLimitAlgorithmFactory.getLimitFromConfig(type,request);
            long alreadyConsumed = newLimit - (long) (remainingRatio * newLimit);
            newAlgorithm = RateLimitAlgorithmFactory.createWithConsumed(type,request,alreadyConsumed);
        }
        buckets.put(clientId, newAlgorithm);

        repository.save(new BucketState(
                clientId,
                type.toString(),
                newAlgorithm.getLimit(),
                request.getRefileRatePerSecond(),
                request.getWindowSizeSeconds(),
                newAlgorithm.getRemainingCapacity()
        ));
    }

    //load before statring the service
    @PostConstruct
    public void loadFromDatabase(){
        repository.findAll().forEach(state ->{
                    AlgorithmType type = AlgorithmType.valueOf(state.getAlgorithmType());
                    ClientConfigRequest request = new ClientConfigRequest(
                            state.getClientId(),
                            state.getLimit(),
                            state.getRefillRatePerSecond(),
                            state.getRemainingCapacity(),
                            state.getWindowSizeSeconds(),
                            type
                    );

                    buckets.put(state.getClientId(), RateLimitAlgorithmFactory.create(type,request));

                }
        );
        System.out.println("Loaded "+ buckets.size() +" buckets from DB");
    }

    //after scheduled time it flush into database
    @Scheduled(fixedDelay = 5000)
    public void flushToDataBase(){
        buckets.forEach((clientId,bucket)->{
                Optional<BucketState> existing = repository.findById(clientId);

                long refillRate = existing.map(BucketState::getRefillRatePerSecond).orElse(0L);
                long windowSize = existing.map(BucketState::getWindowSizeSeconds).orElse(0L);
                repository.save(
                        new BucketState(
                                clientId,
                                bucket.getAlgorithmType().toString(),
                                bucket.getLimit(),
                                refillRate,
                                windowSize,
                                bucket.getRemainingCapacity()
                        )
                );

                }

        );

        System.out.println("Flushed "+buckets.size()+" buckets to DB");
    }
}

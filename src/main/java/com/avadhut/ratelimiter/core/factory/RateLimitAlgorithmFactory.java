package com.avadhut.ratelimiter.core.factory;

import com.avadhut.ratelimiter.core.RateLimitAlgorithm;
import com.avadhut.ratelimiter.core.SlidingWindowCounter;
import com.avadhut.ratelimiter.core.TokenBucket;
import com.avadhut.ratelimiter.entity.AlgorithmType;
import com.avadhut.ratelimiter.model.ClientConfigRequest;

public class RateLimitAlgorithmFactory {

    //get the implementation based on the type
    public static RateLimitAlgorithm create(AlgorithmType type, ClientConfigRequest request){
        return switch (type){
            case TOKEN_BUCKET -> new TokenBucket(request.getMaxTokens(),request.getRefileRatePerSecond());
            case SLIDING_WINDOW -> new SlidingWindowCounter(request.getRequestLimit(),request.getWindowSizeSeconds());
        };
    }

    public static RateLimitAlgorithm createWithConsumed(AlgorithmType type, ClientConfigRequest request, long alreadyConsumed){
        return switch (type){
            case TOKEN_BUCKET -> {
                long remeaning = request.getMaxTokens() - alreadyConsumed;
                yield  new TokenBucket(request.getMaxTokens(), request.getRefileRatePerSecond(), remeaning);
            }
            case SLIDING_WINDOW -> new SlidingWindowCounter(request.getRequestLimit(),request.getWindowSizeSeconds(),alreadyConsumed);
        };
    }

    //to get the limit based on the type
    public static long  getLimitFromConfig(AlgorithmType type, ClientConfigRequest request){
        return switch (type){
            case TOKEN_BUCKET -> request.getMaxTokens();
            case SLIDING_WINDOW -> request.getRequestLimit();
        };
    }
}

package com.avadhut.ratelimiter.core;


import com.avadhut.ratelimiter.entity.AlgorithmType;

public interface RateLimitAlgorithm {
    boolean tryConsume();
    long getRemainingCapacity();
    long getLimit();
    AlgorithmType getAlgorithmType();
}

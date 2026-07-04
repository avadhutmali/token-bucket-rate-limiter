package com.avadhut.ratelimiter.core;


public interface RateLimitAlgorithm {
    boolean tryConsume();
}

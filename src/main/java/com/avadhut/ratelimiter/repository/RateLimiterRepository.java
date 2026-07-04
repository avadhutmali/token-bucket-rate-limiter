package com.avadhut.ratelimiter.repository;

import com.avadhut.ratelimiter.entity.BucketState;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RateLimiterRepository extends JpaRepository<BucketState,Long> {
}

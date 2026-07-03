package com.avadhut.ratelimiter.controller;

import com.avadhut.ratelimiter.model.RateLimitResponse;
import com.avadhut.ratelimiter.service.RateLimitService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class RateLimitController {
    private final RateLimitService service;

    public RateLimitController(RateLimitService service){
        this.service = service;
    }

    @GetMapping("/check")
    public ResponseEntity<RateLimitResponse> check(@RequestParam String clientId){
        RateLimitResponse response = service.checkLimit(clientId);

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-RateLimit-Limit", String.valueOf(response.getLimit()));
        headers.set("X-RateLimit-Remaining",String.valueOf(response.getRemaining()));
        headers.set("X-RateLimit-Reset", String.valueOf(response.getResetAfterSeconds()));

        int statusCode = response.getDecisision().equals("ALLOW") ? 200 : 429;

        return ResponseEntity
                .status(statusCode)
                .headers(headers)
                .body(response);

    }
}

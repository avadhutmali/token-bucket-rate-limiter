package com.avadhut.ratelimiter.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
public class HomeController {

    @GetMapping("/")
    public Map<String, String> home() {
        Map<String, String> info = new LinkedHashMap<>();
        info.put("service", "Token Bucket Rate Limiter");
        info.put("try_it", "/check?clientId=yourname");
        info.put("admin_config", "POST /admin/config");
        info.put("github", "https://github.com/avadhutmali/token-bucket-rate-limiter");
        return info;
    }
}
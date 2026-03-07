package com.jeet.apigateway.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class RateLimitStatusResponse {
    private final String clientId;
    private final long tokensRemaining;
    private final long capacity;
    private final long refillTokens;
    private final long refillDurationSeconds;

}
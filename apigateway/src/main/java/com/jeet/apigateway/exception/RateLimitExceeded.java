package com.jeet.apigateway.exception;

public class RateLimitExceeded extends RuntimeException {
    public RateLimitExceeded(String message) {
        super(message);
    }
}

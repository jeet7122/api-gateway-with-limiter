package com.jeet.apigateway.ratelimit;

import com.jeet.apigateway.exception.RateLimitExceeded;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class RateLimitService {

    private final StringRedisTemplate redisTemplate;

    @Value("${rate-limit.capacity}")
    private long capacity;

    @Value("${rate-limit.refill-tokens}")
    private long refillTokens;

    @Value("${rate-limit.refill-duration-seconds}")
    private long refillDurationSeconds;

    public void checkRateLimit(String clientId) {
        String key = "ratelimit:" + clientId;
        long now = Instant.now().getEpochSecond();

        TokenBucket bucket = getBucket(key, now);
        refillTokens(bucket, now);

        if (bucket.getTokens() <= 0) {
            throw new RateLimitExceeded("Rate limit exceeded. Please try again later.");
        }

        bucket.setTokens(bucket.getTokens() - 1);
        saveBucket(key, bucket);
    }

    private TokenBucket getBucket(String key, long now) {
        Map<Object, Object> entries = redisTemplate.opsForHash().entries(key);

        if (entries == null || entries.isEmpty()) {
            return new TokenBucket(capacity, now);
        }

        long tokens = Long.parseLong(entries.getOrDefault("tokens", String.valueOf(capacity)).toString());
        long lastRefillTimestamp = Long.parseLong(entries.getOrDefault("lastRefillTimestamp", String.valueOf(now)).toString());

        return new TokenBucket(tokens, lastRefillTimestamp);
    }

    private void refillTokens(TokenBucket bucket, long now) {
        long lastRefillTime = bucket.getLastRefillTimestamp();
        long elapsedSeconds = now - lastRefillTime;

        if (elapsedSeconds <= 0) {
            return;
        }

        long tokensToAdd = (elapsedSeconds * refillTokens) / refillDurationSeconds;

        if (tokensToAdd > 0) {
            long newTokenCount = Math.min(capacity, bucket.getTokens() + tokensToAdd);
            bucket.setTokens(newTokenCount);

            long consumedRefillTime = (tokensToAdd * refillDurationSeconds) / refillTokens;
            bucket.setLastRefillTimestamp(lastRefillTime + consumedRefillTime);
        }
    }

    private void saveBucket(String key, TokenBucket bucket) {
        redisTemplate.opsForHash().put(key, "tokens", String.valueOf(bucket.getTokens()));
        redisTemplate.opsForHash().put(key, "lastRefillTimestamp", String.valueOf(bucket.getLastRefillTimestamp()));

        redisTemplate.expire(key, refillDurationSeconds * 2, TimeUnit.SECONDS);
    }
}
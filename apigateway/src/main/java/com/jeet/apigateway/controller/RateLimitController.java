package com.jeet.apigateway.controller;

import com.jeet.apigateway.model.RateLimitStatusResponse;
import com.jeet.apigateway.ratelimit.RateLimitService;
import com.jeet.apigateway.ratelimit.TokenBucket;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/private/rate-limit")
public class RateLimitController {
    private final RateLimitService rateLimitService;

    @GetMapping("/status")
    public RateLimitStatusResponse getRateLimitStatus(HttpServletRequest request){

        String clientId = resolveClientId(request);

        TokenBucket bucket = rateLimitService.getCurrentBucket(clientId);

        return new RateLimitStatusResponse(
                clientId,
                bucket.getTokens(),
                rateLimitService.getCapacity(),
                rateLimitService.getRefillTokens(),
                rateLimitService.getRefillDurationSeconds()
        );

    }

    private String resolveClientId(HttpServletRequest request){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getPrincipal())){
            return authentication.getName();
        }

        return request.getRemoteAddr();
    }


}

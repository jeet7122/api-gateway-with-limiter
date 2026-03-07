package com.jeet.apigateway.ratelimit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jeet.apigateway.exception.RateLimitExceeded;
import com.jeet.apigateway.model.ApiErrorResponse;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class RateLimitFilter extends OncePerRequestFilter {

    private final RateLimitService rateLimitService;
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String clientId = request.getRemoteAddr();

        try {
            rateLimitService.checkRateLimit(clientId);
            filterChain.doFilter(request, response);
        } catch (RateLimitExceeded e) {
            ApiErrorResponse errorResponse = new ApiErrorResponse(
                    LocalDateTime.now(),
                    429,
                    "Too Many Requests",
                    e.getMessage(),
                    request.getRequestURI()
            );

            response.setStatus(429);
            response.setContentType("application/json");
            response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
        }
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/api/public/")
                || path.equals("/api/health");
    }
}
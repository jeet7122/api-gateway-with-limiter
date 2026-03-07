package com.jeet.apigateway.controller;

import com.jeet.apigateway.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/public")
@RequiredArgsConstructor
public class AuthController {
    private final JwtService jwtService;

    @GetMapping("/token")
    public String generateToken(@RequestParam String username){
        return jwtService.generateToken(username);
    }
}

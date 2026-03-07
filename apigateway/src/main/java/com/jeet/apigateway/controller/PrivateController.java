package com.jeet.apigateway.controller;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/private")
public class PrivateController {

    @GetMapping("/hello")
    public String greet(Authentication authentication){
        return "Hello from protected endpoint, user: " + authentication.getName();
    }
}

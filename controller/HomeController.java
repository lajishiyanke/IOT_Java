package com.iot.platform.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeController {
    
    @GetMapping("/")
    public String home() {
        return "Welcome to IOT Platform!";
    }
    
    @GetMapping("/test")
    public String test() {
        return "Test endpoint is working!";
    }
} 
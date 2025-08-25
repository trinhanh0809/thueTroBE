package com.example.DATN.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/debug")
public class DebugController {
    @GetMapping("/whoami")
    public Map<String, Object> whoami(org.springframework.security.core.Authentication auth) {
        if (auth == null) return Map.of("auth", null);
        return Map.of(
                "name", auth.getName(),
                "authorities", auth.getAuthorities().stream().map(Object::toString).toList()
        );
    }
}

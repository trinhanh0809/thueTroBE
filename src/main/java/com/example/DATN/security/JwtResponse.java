package com.example.DATN.security;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class JwtResponse {
    private final String jwtToken;

    public String getJwtToken() {
        return jwtToken;
    }

}

package com.example.DATN.service;

import com.example.DATN.entity.User;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.Optional;

public interface UserSecurityService extends UserDetailsService {
    Optional<User> findByUsername(String username);
}

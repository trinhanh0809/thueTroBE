package com.example.DATN.dao;


import com.example.DATN.entity.RefreshToken;
import com.example.DATN.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByToken(String token);
    long deleteByUser(User user);
}

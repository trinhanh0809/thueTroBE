package com.example.DATN.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity @Table(name = "refresh_token")
public class RefreshToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "token", length = 128, unique = true, nullable = false)
    private String token;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "user_id", referencedColumnName = "id_user", nullable = false)
    private User user;

    @Column(name = "expires_at", nullable = false) private Instant expiresAt;
    @Column(name = "revoked", nullable = false) private boolean revoked = false;
    @Column(name = "created_at", nullable = false) private Instant createdAt;

    @PrePersist void onCreate(){ createdAt = Instant.now(); }
}
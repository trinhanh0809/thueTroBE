package com.example.DATN.entity;

import jakarta.persistence.*;

@Entity
public class BlockImage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name="block_id", nullable=false)
    private Block block;

    @Column(nullable=false, length=500) private String url;
    @Column(nullable=false) private boolean cover = false;
    @Column(nullable=false) private int sortOrder = 0;
}


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
@Entity
@Table(name = "room_image")
public class RoomImage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "room_id", referencedColumnName = "id_room", nullable = false)
    private Room room;

    @Column(name = "url", nullable = false) private String url;
    @Column(name = "is_cover") private Boolean cover = false;
    @Column(name = "sort_order") private Integer sortOrder;
    @Column(name = "uploaded_at") private Instant uploadedAt;

    @PrePersist void onCreate(){ uploadedAt = Instant.now(); }
}
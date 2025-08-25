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
@Table(name = "favorite")
public class Favorite {
    @EmbeddedId
    private FavoriteId id = new FavoriteId();

    @ManyToOne(fetch = FetchType.LAZY) @MapsId("userId")
    @JoinColumn(name = "user_id", referencedColumnName = "id_user")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY) @MapsId("roomId")
    @JoinColumn(name = "room_id", referencedColumnName = "id_room")
    private Room room;

    @Column(name = "created_at") private Instant createdAt;
    @PrePersist void onCreate(){ createdAt = Instant.now(); }
}

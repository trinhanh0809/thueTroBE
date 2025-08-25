package com.example.DATN.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "room_amenity")
public class RoomAmenity {
    @EmbeddedId
    private RoomAmenityId id = new RoomAmenityId();

    @ManyToOne(fetch = FetchType.LAZY) @MapsId("roomId")
    @JoinColumn(name = "room_id", referencedColumnName = "id_room")
    private Room room;

    @ManyToOne(fetch = FetchType.LAZY) @MapsId("amenityId")
    @JoinColumn(name = "amenity_id")
    private Amenity amenity;
}
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
@Entity @Table(name = "room_type")
public class RoomType {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "code", unique = true, nullable = false, length = 32)
    private String code;

    @Column(name = "name", nullable = false, length = 128)
    private String name;

    @Column(name = "sort_order") private Integer sortOrder;
    @Column(name = "active") private Boolean active = true;
}

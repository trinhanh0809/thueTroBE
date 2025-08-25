package com.example.DATN.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class FavoriteId implements Serializable {
    @Column(name = "user_id") private Integer userId;
    @Column(name = "room_id") private Long roomId;
}

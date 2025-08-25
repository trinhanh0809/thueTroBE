package com.example.DATN.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity @Table(name = "role")
public class Role {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_role")
    private Long idRole;

    @Column(name = "name_role", unique = true, nullable = false)
    private String nameRole; // ADMIN, CUSTOMER, HOST
}

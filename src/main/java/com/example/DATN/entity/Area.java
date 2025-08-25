package com.example.DATN.entity;

import com.example.DATN.enums.AreaType;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"parent","children"})
@Entity
@Table(name = "area")
public class Area {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, length = 128)
    private String name;

    @Column(name = "code", length = 32) private String code;

    @Enumerated(EnumType.STRING) @Column(name = "type", nullable = false, length = 16)
    private AreaType type;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "parent_id")
    private Area parent;

    @OneToMany(mappedBy = "parent") private List<Area> children = new ArrayList<>();
}

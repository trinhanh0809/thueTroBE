package com.example.DATN.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"contract"})
@Entity
@Table(name = "meter_reading",
        uniqueConstraints = @UniqueConstraint(columnNames = {"contract_id","period"}))
public class MeterReading {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "contract_id", nullable = false)
    private Contract contract;

    @Column(name = "period", nullable = false) private LocalDate period; // ví dụ 2025-08-01

    @Column(name = "elec_prev")  private Integer elecPrev;
    @Column(name = "elec_curr")  private Integer elecCurr;
    @Column(name = "water_prev") private Integer waterPrev;
    @Column(name = "water_curr") private Integer waterCurr;

    @Column(name = "created_at") private Instant createdAt;
    @PrePersist void onCreate(){ createdAt = Instant.now(); }
}
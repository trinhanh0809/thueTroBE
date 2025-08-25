package com.example.DATN.entity;

import com.example.DATN.enums.RoomStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
@Entity @Table(name = "room")
public class Room {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_room")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "host_id", referencedColumnName = "id_user", nullable = false)
    private User host;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "room_type_id")
    private RoomType roomType;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "area_id") // trỏ tới WARD
    private Area area;

    @Column(name = "address_line") private String addressLine;

    @Column(name = "price_month", precision = 10, scale = 2) private BigDecimal priceMonth;
    @Column(name = "deposit", precision = 10, scale = 2) private BigDecimal deposit;
    @Column(name = "electricity_price", precision = 10, scale = 2) private BigDecimal electricityPrice;
    @Column(name = "water_price", precision = 10, scale = 2) private BigDecimal waterPrice;

    @Column(name = "area_sqm") private Integer areaSqm;
    @Column(name = "max_occupancy") private Integer maxOccupancy;

    @Column(name = "title") private String title;
    @Lob @Column(name = "description") private String description;

    @Enumerated(EnumType.STRING) @Column(name = "status", length = 16) private RoomStatus status = RoomStatus.DRAFT;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "approved_by", referencedColumnName = "id_user")
    private User approvedBy;
    @Column(name = "approved_at") private Instant approvedAt;

    @Column(name = "lat") private Double lat;
    @Column(name = "lng") private Double lng;

    @Column(name = "created_at") private Instant createdAt;
    @Column(name = "updated_at") private Instant updatedAt;
    @PrePersist void onCreate(){ createdAt = Instant.now(); updatedAt = createdAt; }
    @PreUpdate  void onUpdate(){ updatedAt = Instant.now(); }
}

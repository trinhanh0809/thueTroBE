package com.example.DATN.entity;

import com.example.DATN.enums.RoomStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
        name = "room",
        indexes = {
                @Index(name = "idx_room_host_id",  columnList = "host_id"),
                @Index(name = "idx_room_block_id", columnList = "block_id"),
                @Index(name = "idx_room_type_id",  columnList = "room_type_id"),
                @Index(name = "idx_room_area_id",  columnList = "area_id")
        }
)
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_room")
    private Long id;

    // Chủ phòng (bắt buộc) – KHÔNG tạo FK ở DB để tránh nổ khi dữ liệu cũ bẩn
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "host_id",
            referencedColumnName = "id_user",
            nullable = false,
            foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT)
    )
    private User host;

    // Thuộc dãy (bắt buộc) – KHÔNG tạo FK ở DB
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "block_id",
            nullable = false,
            foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT)
            // Khi dữ liệu sạch, đổi lại:
            // foreignKey = @ForeignKey(name = "fk_room_block")
    )
    private Block block;

    // Loại phòng (global, bắt buộc) – KHÔNG tạo FK ở DB
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "room_type_id",
            nullable = false,
            foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT)
            // Khi dữ liệu sạch, đổi lại:
            // foreignKey = @ForeignKey(name = "fk_room_room_type")
    )
    private RoomType roomType;

    // Khu vực hành chính (tuỳ chọn) – KHÔNG tạo FK ở DB
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "area_id",
            foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT)
            // Khi dữ liệu sạch, đổi lại:
            // foreignKey = @ForeignKey(name = "fk_room_area")
    )
    private Area area;

    @Column(name = "address_line")
    private String addressLine;

    @Column(name = "price_month", precision = 10, scale = 2)
    private BigDecimal priceMonth;

    @Column(name = "deposit", precision = 10, scale = 2)
    private BigDecimal deposit;

    @Column(name = "electricity_price", precision = 10, scale = 2)
    private BigDecimal electricityPrice;

    @Column(name = "water_price", precision = 10, scale = 2)
    private BigDecimal waterPrice;

    @Column(name = "area_sqm")
    private Integer areaSqm;

    @Column(name = "max_occupancy")
    private Integer maxOccupancy;

    @Column(name = "title")
    private String title;

    @Lob
    @Column(name = "description")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 16)
    private RoomStatus status = RoomStatus.DRAFT;

    // Người duyệt (tuỳ chọn) – KHÔNG tạo FK ở DB
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "approved_by",
            referencedColumnName = "id_user",
            foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT)
            // Khi dữ liệu sạch, đổi lại:
            // foreignKey = @ForeignKey(name = "fk_room_approved_by")
    )
    private User approvedBy;

    @Column(name = "approved_at")
    private Instant approvedAt;

    @Column(name = "lat")
    private Double lat;

    @Column(name = "lng")
    private Double lng;

    @Column(name = "created_at")
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;
    @ManyToMany
    @JoinTable(
            name = "room_object",
            joinColumns = @JoinColumn(name = "room_id", referencedColumnName = "id_room",
                    foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT)),
            inverseJoinColumns = @JoinColumn(name = "object_id", referencedColumnName = "id",
                    foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    )
    private Set<RoomObject> objects = new LinkedHashSet<>();
    @ManyToMany
    @JoinTable(
            name = "room_environment",
            joinColumns = @JoinColumn(name = "room_id", referencedColumnName = "id_room",
                    foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT)),
            inverseJoinColumns = @JoinColumn(name = "environment_id", referencedColumnName = "id",
                    foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    )
    private Set<Environment> environments = new LinkedHashSet<>();

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }
}

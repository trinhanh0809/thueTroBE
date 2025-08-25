package com.example.DATN.entity;

import com.example.DATN.enums.BookingRequestStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"room","tenant","approvedBy"})
@Entity
@Table(name = "booking_request")
public class BookingRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "room_id", referencedColumnName = "id_room", nullable = false)
    private Room room;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "tenant_id", referencedColumnName = "id_user", nullable = false)
    private User tenant;

    @Enumerated(EnumType.STRING) @Column(length = 16, nullable = false)
    private BookingRequestStatus status = BookingRequestStatus.PENDING;

    @Column(name = "schedule_at") private LocalDateTime scheduleAt;
    @Column(name = "note") private String note;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "approved_by", referencedColumnName = "id_user")
    private User approvedBy;

    @Column(name = "approved_at") private Instant approvedAt;
    @Column(name = "created_at")  private Instant createdAt;
    @Column(name = "updated_at")  private Instant updatedAt;

    @PrePersist void onCreate(){ createdAt = Instant.now(); updatedAt = createdAt; }
    @PreUpdate  void onUpdate(){ updatedAt = Instant.now(); }
}
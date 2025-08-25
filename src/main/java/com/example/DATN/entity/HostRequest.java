package com.example.DATN.entity;


import com.example.DATN.enums.HostRequestStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"user","approvedBy"})
@Entity @Table(name = "host_request")
public class HostRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "user_id", referencedColumnName = "id_user", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING) @Column(length = 16, nullable = false)
    private HostRequestStatus status = HostRequestStatus.PENDING;

    @Column(name = "note", length = 500) private String note;
    @Lob @Column(name = "reason") private String reason;        // lý do reject / ghi chú

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "approved_by", referencedColumnName = "id_user")
    private User approvedBy;

    @Column(name = "approved_at") private Instant approvedAt;
    @Column(name = "created_at")  private Instant createdAt;
    @Column(name = "updated_at")  private Instant updatedAt;

    @PrePersist void onCreate(){ createdAt = Instant.now(); updatedAt = createdAt; }
    @PreUpdate  void onUpdate(){ updatedAt = Instant.now(); }
}

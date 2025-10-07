package com.example.DATN.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(
        name = "tenant",
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_tenant_phone", columnNames = "phone")
        },
        indexes = {
                @Index(name = "idx_tenant_full_name", columnList = "full_name"),
                @Index(name = "idx_tenant_phone", columnList = "phone")
        }
)
public class Tenant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 150)
    @Column(name = "full_name", nullable = false, length = 150)
    private String fullName;

    @NotBlank
    @Size(max = 30)
    @Column(name = "phone", nullable = false, length = 30, unique = true)
    private String phone;

    @Email
    @Size(max = 120)
    @Column(length = 120)
    private String email;

    @Size(max = 50)
    @Column(name = "id_number", length = 50)
    private String idNumber;

    @Column(name = "dob")
    private LocalDate dob;

    @Size(max = 255)
    @Column(name = "address_perm", length = 255)
    private String addressPerm;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String note;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "tenant", fetch = FetchType.LAZY,
            cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    @Builder.Default
    private List<Contract> contracts = new ArrayList<>();


}

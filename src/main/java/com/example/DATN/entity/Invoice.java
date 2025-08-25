package com.example.DATN.entity;

import com.example.DATN.enums.InvoiceStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"contract","items"})
@Entity
@Table(name = "invoice",
        uniqueConstraints = @UniqueConstraint(columnNames = {"contract_id","period"}))
public class Invoice {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "contract_id", nullable = false)
    private Contract contract;

    @Column(name = "period", nullable = false) private LocalDate period;
    @Column(name = "due_date") private LocalDate dueDate;

    @Enumerated(EnumType.STRING) @Column(length = 16, nullable = false)
    private InvoiceStatus status = InvoiceStatus.ISSUED;

    @Column(name = "sub_total", precision = 12, scale = 2) private BigDecimal subTotal;
    @Column(name = "total", precision = 12, scale = 2) private BigDecimal total;

    @Column(name = "issued_at") private Instant issuedAt;
    @Column(name = "paid_at")   private Instant paidAt;
    @Column(name = "note")      private String note;

    @OneToMany(mappedBy = "invoice", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<InvoiceItem> items = new ArrayList<>();
}
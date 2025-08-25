package com.example.DATN.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"invoice"})
@Entity
@Table(name = "invoice_item")
public class InvoiceItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "invoice_id", nullable = false)
    private Invoice invoice;

    @Column(name = "name", nullable = false) private String name;     // "Tiền phòng", "Điện", "Nước", ...
    @Column(name = "qty", precision = 12, scale = 2) private BigDecimal qty;
    @Column(name = "unit_price", precision = 12, scale = 2) private BigDecimal unitPrice;
    @Column(name = "amount", precision = 12, scale = 2) private BigDecimal amount;
}

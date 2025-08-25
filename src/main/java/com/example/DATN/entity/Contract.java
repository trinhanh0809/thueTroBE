package com.example.DATN.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"room","tenant"})
@Entity
@Table(name = "contract")
public class Contract {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "room_id", referencedColumnName = "id_room", nullable = false)
    private Room room;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "tenant_id", referencedColumnName = "id_user", nullable = false)
    private User tenant;

    @Column(name = "start_date", nullable = false) private LocalDate startDate;
    @Column(name = "end_date") private LocalDate endDate;

    @Column(name = "bill_day") private Integer billDay; // ngày chốt hoá đơn

    // snapshot đơn giá tại thời điểm ký
    @Column(name = "price_month", precision = 10, scale = 2) private BigDecimal priceMonth;
    @Column(name = "elec_price", precision = 10, scale = 2) private BigDecimal elecPrice;
    @Column(name = "water_price", precision = 10, scale = 2) private BigDecimal waterPrice;

    @Column(name = "active", nullable = false) private Boolean active = true;
}

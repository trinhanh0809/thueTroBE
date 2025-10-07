package com.example.DATN.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

import static jakarta.persistence.FetchType.LAZY;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"room","tenant"})
@Entity
@Table(name = "contract")
public class Contract {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Nếu Room có PK là "id", hãy bỏ referencedColumnName đi.
    @ManyToOne(fetch = LAZY, optional = false)
    @JoinColumn(
            name = "room_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_contract_room")
    )
    private Room room;

    // SỬA Ở ĐÂY: trỏ đúng sang Tenant (PK "id")
    @ManyToOne(fetch = LAZY, optional = false)
    @JoinColumn(name = "tenant_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_contract_tenant"))
    private Tenant tenant;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    // Ngày chốt hoá đơn (1..31)
    @Min(1) @Max(31)
    @Column(name = "bill_day")
    private Integer billDay;

    // snapshot đơn giá tại thời điểm ký
    @Column(name = "price_month", precision = 18, scale = 2)
    private BigDecimal priceMonth;

    @Column(name = "elec_price", precision = 18, scale = 2)
    private BigDecimal elecPrice;

    @Column(name = "water_price", precision = 18, scale = 2)
    private BigDecimal waterPrice;

    @Column(name = "active", nullable = false)
    private Boolean active = true;
}

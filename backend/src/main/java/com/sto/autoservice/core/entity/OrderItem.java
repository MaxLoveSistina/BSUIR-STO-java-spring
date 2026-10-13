package com.sto.autoservice.core.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * Позиция заказа: одна услуга с выбранными материалами и сроком.
 * Название, материал и цены копируются в позицию (снимок на момент заказа),
 * поэтому последующее изменение каталога мастера не меняет уже оформленные заказы.
 */
@Entity
@Table(name = "order_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private RepairOrder order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_id")
    private RepairService service;

    @Column(name = "service_name", nullable = false, length = 150)
    private String serviceName;

    @Enumerated(EnumType.STRING)
    @Column(name = "material_type", nullable = false, length = 20)
    private MaterialType materialType;

    @Column(name = "material_name", nullable = false, length = 150)
    private String materialName;

    @Enumerated(EnumType.STRING)
    @Column(name = "deadline_type", nullable = false, length = 20)
    private DeadlineType deadlineType;

    /** Стоимость работы с учётом коэффициента срока. */
    @Column(name = "labor_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal laborPrice;

    @Column(name = "material_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal materialPrice;

    /** Итого по позиции: работа + материалы. */
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @Column(name = "duration_days", nullable = false)
    private int durationDays;
}

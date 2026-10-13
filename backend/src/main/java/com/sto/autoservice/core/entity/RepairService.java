package com.sto.autoservice.core.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

/**
 * Услуга из каталога конкретного мастера. У каждого мастера свой набор услуг,
 * свои цены, свои материалы и свои базовые сроки.
 */
@Entity
@Table(name = "repair_services")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RepairService {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Мастер — владелец услуги. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "master_id", nullable = false)
    private User master;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(length = 1000)
    private String description;

    /** Базовая стоимость работы (при стандартном сроке). */
    @Column(name = "labor_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal laborPrice;

    /** Базовая длительность работы в днях (при стандартном сроке). */
    @Column(name = "duration_days", nullable = false)
    private int durationDays;

    @Column(name = "economy_material_name", nullable = false, length = 150)
    private String economyMaterialName;

    @Column(name = "economy_material_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal economyMaterialPrice;

    @Column(name = "premium_material_name", nullable = false, length = 150)
    private String premiumMaterialName;

    @Column(name = "premium_material_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal premiumMaterialPrice;

    /** Доступна ли услуга клиентам для заказа. */
    @Builder.Default
    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    /** Мягкое удаление: услуга скрыта у мастера и клиентов, но остаётся в истории заказов. */
    @Builder.Default
    @Column(name = "is_deleted", nullable = false)
    private boolean deleted = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = OffsetDateTime.now(ZoneOffset.UTC);
    }

    public String materialNameFor(MaterialType type) {
        return type == MaterialType.PREMIUM ? premiumMaterialName : economyMaterialName;
    }

    public BigDecimal materialPriceFor(MaterialType type) {
        return type == MaterialType.PREMIUM ? premiumMaterialPrice : economyMaterialPrice;
    }
}

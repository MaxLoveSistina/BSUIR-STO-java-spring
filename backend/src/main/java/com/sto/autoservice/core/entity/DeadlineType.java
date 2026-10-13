package com.sto.autoservice.core.entity;

import java.math.BigDecimal;

/**
 * Срок выполнения работы. Чем быстрее срок — тем выше коэффициент к стоимости работы
 * и тем меньше коэффициент длительности.
 * <p>
 * priceMultiplier умножает стоимость работы мастера (материалы не умножаются),
 * durationFactor умножает базовую длительность услуги в днях (результат округляется вверх, минимум 1 день).
 */
public enum DeadlineType {

    STANDARD("Стандартный", "1.00", "1.00"),
    EXPRESS("Ускоренный", "1.30", "0.50"),
    URGENT("Срочный", "1.60", "0.25");

    private final String label;
    private final BigDecimal priceMultiplier;
    private final BigDecimal durationFactor;

    DeadlineType(String label, String priceMultiplier, String durationFactor) {
        this.label = label;
        this.priceMultiplier = new BigDecimal(priceMultiplier);
        this.durationFactor = new BigDecimal(durationFactor);
    }

    public String getLabel() {
        return label;
    }

    public BigDecimal getPriceMultiplier() {
        return priceMultiplier;
    }

    public BigDecimal getDurationFactor() {
        return durationFactor;
    }
}

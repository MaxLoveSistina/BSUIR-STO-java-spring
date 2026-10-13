package com.sto.autoservice.core.pricing;

import com.sto.autoservice.core.entity.DeadlineType;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Расчёт стоимости одной позиции заказа.
 * <pre>
 * работа  = базовая_цена_работы × коэффициент_срока
 * итого   = работа + цена_выбранных_материалов
 * дни     = max(1, ceil(базовые_дни × коэффициент_длительности))
 * </pre>
 * Класс намеренно не зависит от Spring, чтобы его легко было тестировать.
 */
public class PriceCalculator {

    public ItemPrice calculate(BigDecimal baseLaborPrice,
                               int baseDurationDays,
                               BigDecimal materialPrice,
                               DeadlineType deadline) {
        if (baseLaborPrice == null || materialPrice == null || deadline == null) {
            throw new IllegalArgumentException("Цена работы, цена материалов и срок обязательны");
        }
        if (baseLaborPrice.signum() < 0 || materialPrice.signum() < 0) {
            throw new IllegalArgumentException("Цены не могут быть отрицательными");
        }
        if (baseDurationDays < 1) {
            throw new IllegalArgumentException("Длительность услуги должна быть не меньше 1 дня");
        }

        BigDecimal labor = baseLaborPrice
                .multiply(deadline.getPriceMultiplier())
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal material = materialPrice.setScale(2, RoundingMode.HALF_UP);
        BigDecimal total = labor.add(material);

        int days = BigDecimal.valueOf(baseDurationDays)
                .multiply(deadline.getDurationFactor())
                .setScale(0, RoundingMode.CEILING)
                .intValueExact();

        return new ItemPrice(labor, material, total, Math.max(1, days));
    }

    /**
     * Результат расчёта одной позиции.
     */
    public record ItemPrice(BigDecimal laborPrice,
                            BigDecimal materialPrice,
                            BigDecimal totalPrice,
                            int durationDays) {
    }
}

package com.sto.autoservice.core.pricing;

import com.sto.autoservice.core.entity.DeadlineType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PriceCalculatorTest {

    private final PriceCalculator calculator = new PriceCalculator();

    @Test
    @DisplayName("Стандартный срок: коэффициенты равны 1")
    void standardDeadline() {
        var price = calculator.calculate(new BigDecimal("1000"), 2, new BigDecimal("500"), DeadlineType.STANDARD);

        assertThat(price.laborPrice()).isEqualByComparingTo("1000.00");
        assertThat(price.materialPrice()).isEqualByComparingTo("500.00");
        assertThat(price.totalPrice()).isEqualByComparingTo("1500.00");
        assertThat(price.durationDays()).isEqualTo(2);
    }

    @Test
    @DisplayName("Ускоренный срок: работа ×1.3, срок ×0.5, материалы не умножаются")
    void expressDeadline() {
        var price = calculator.calculate(new BigDecimal("1000"), 2, new BigDecimal("1500"), DeadlineType.EXPRESS);

        assertThat(price.laborPrice()).isEqualByComparingTo("1300.00");
        assertThat(price.materialPrice()).isEqualByComparingTo("1500.00");
        assertThat(price.totalPrice()).isEqualByComparingTo("2800.00");
        assertThat(price.durationDays()).isEqualTo(1);
    }

    @Test
    @DisplayName("Срочный срок: работа ×1.6")
    void urgentDeadline() {
        var price = calculator.calculate(new BigDecimal("2500"), 4, new BigDecimal("5500"), DeadlineType.URGENT);

        assertThat(price.laborPrice()).isEqualByComparingTo("4000.00");
        assertThat(price.totalPrice()).isEqualByComparingTo("9500.00");
        assertThat(price.durationDays()).isEqualTo(1);
    }

    @Test
    @DisplayName("Дни округляются вверх, но не меньше 1")
    void daysRoundedUpWithMinimumOfOne() {
        assertThat(calculator.calculate(new BigDecimal("1"), 5, BigDecimal.ZERO, DeadlineType.EXPRESS).durationDays())
                .isEqualTo(3);
        assertThat(calculator.calculate(new BigDecimal("1"), 1, BigDecimal.ZERO, DeadlineType.URGENT).durationDays())
                .isEqualTo(1);
    }

    @Test
    @DisplayName("Стоимость работы округляется до копеек (HALF_UP)")
    void laborRoundedToCents() {
        var price = calculator.calculate(new BigDecimal("333.33"), 1, BigDecimal.TEN, DeadlineType.EXPRESS);

        assertThat(price.laborPrice()).isEqualByComparingTo("433.33");
    }

    @Test
    @DisplayName("Некорректные входные данные отклоняются")
    void invalidInput() {
        assertThatThrownBy(() -> calculator.calculate(BigDecimal.ONE, 0, BigDecimal.ZERO, DeadlineType.STANDARD))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> calculator.calculate(new BigDecimal("-1"), 1, BigDecimal.ZERO, DeadlineType.STANDARD))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> calculator.calculate(BigDecimal.ONE, 1, BigDecimal.ZERO, null))
                .isInstanceOf(IllegalArgumentException.class);
    }
}

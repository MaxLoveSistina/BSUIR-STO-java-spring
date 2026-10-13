package com.sto.autoservice.web.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/**
 * Создание / изменение услуги в каталоге мастера.
 */
public record ServiceRequest(
        @NotBlank(message = "Название услуги не может быть пустым")
        @Size(max = 150, message = "Название не длиннее 150 символов")
        String name,

        @Size(max = 1000, message = "Описание не длиннее 1000 символов")
        String description,

        @NotNull(message = "Укажите стоимость работы")
        @DecimalMin(value = "0.01", message = "Стоимость работы должна быть больше 0")
        @DecimalMax(value = "9999999.99", message = "Слишком большая стоимость работы")
        BigDecimal laborPrice,

        @Min(value = 1, message = "Срок выполнения — минимум 1 день")
        @Max(value = 365, message = "Срок выполнения — максимум 365 дней")
        int durationDays,

        @NotBlank(message = "Укажите название дешёвых материалов")
        @Size(max = 150, message = "Название материалов не длиннее 150 символов")
        String economyMaterialName,

        @NotNull(message = "Укажите цену дешёвых материалов")
        @DecimalMin(value = "0.00", message = "Цена материалов не может быть отрицательной")
        @DecimalMax(value = "9999999.99", message = "Слишком большая цена материалов")
        BigDecimal economyMaterialPrice,

        @NotBlank(message = "Укажите название дорогих материалов")
        @Size(max = 150, message = "Название материалов не длиннее 150 символов")
        String premiumMaterialName,

        @NotNull(message = "Укажите цену дорогих материалов")
        @DecimalMin(value = "0.00", message = "Цена материалов не может быть отрицательной")
        @DecimalMax(value = "9999999.99", message = "Слишком большая цена материалов")
        BigDecimal premiumMaterialPrice,

        /** Если не указано — услуга активна. */
        Boolean active
) {
}

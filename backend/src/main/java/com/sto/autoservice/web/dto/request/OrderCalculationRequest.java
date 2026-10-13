package com.sto.autoservice.web.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * Предварительный расчёт стоимости (заказ не создаётся).
 */
public record OrderCalculationRequest(
        @NotNull(message = "Выберите мастера")
        Long masterId,

        @NotEmpty(message = "Выберите хотя бы одну услугу")
        List<@Valid @NotNull(message = "Позиция не может быть пустой") OrderItemRequest> items
) {
}

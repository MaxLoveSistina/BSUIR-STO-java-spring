package com.sto.autoservice.web.dto.request;

import com.sto.autoservice.core.entity.DeadlineType;
import com.sto.autoservice.core.entity.MaterialType;
import jakarta.validation.constraints.NotNull;

/**
 * Одна выбранная клиентом услуга с выбором материалов и срока.
 */
public record OrderItemRequest(
        @NotNull(message = "Укажите услугу")
        Long serviceId,

        @NotNull(message = "Выберите материалы: ECONOMY или PREMIUM")
        MaterialType materialType,

        @NotNull(message = "Выберите срок: STANDARD, EXPRESS или URGENT")
        DeadlineType deadlineType
) {
}

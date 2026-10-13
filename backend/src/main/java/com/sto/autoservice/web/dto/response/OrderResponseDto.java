package com.sto.autoservice.web.dto.response;

import com.sto.autoservice.core.entity.OrderStatus;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

public record OrderResponseDto(
        Long id,
        OrderStatus status,
        Long clientId,
        String clientName,
        String clientPhone,
        Long masterId,
        String masterName,
        String carBrand,
        String carModel,
        String plateNumber,
        String comment,
        List<OrderItemResponseDto> items,
        BigDecimal totalPrice,
        int totalDurationDays,
        OffsetDateTime createdAt
) {
}

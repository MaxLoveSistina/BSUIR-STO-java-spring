package com.sto.autoservice.web.dto.response;

import java.math.BigDecimal;
import java.util.List;

public record CalculationResponseDto(
        Long masterId,
        List<OrderItemResponseDto> items,
        BigDecimal totalPrice,
        int totalDurationDays
) {
}

package com.sto.autoservice.web.dto.response;

import java.math.BigDecimal;

public record ClientResponseDto(
        Long id,
        String fullName,
        String phone,
        String email,
        long ordersCount,
        BigDecimal totalSpent
) {
}

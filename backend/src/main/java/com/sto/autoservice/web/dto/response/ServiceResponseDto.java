package com.sto.autoservice.web.dto.response;

import java.math.BigDecimal;

public record ServiceResponseDto(
        Long id,
        Long masterId,
        String masterName,
        String name,
        String description,
        BigDecimal laborPrice,
        int durationDays,
        String economyMaterialName,
        BigDecimal economyMaterialPrice,
        String premiumMaterialName,
        BigDecimal premiumMaterialPrice,
        boolean active
) {
}

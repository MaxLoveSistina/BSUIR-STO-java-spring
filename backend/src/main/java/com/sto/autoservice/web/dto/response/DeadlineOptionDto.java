package com.sto.autoservice.web.dto.response;

import com.sto.autoservice.core.entity.DeadlineType;

import java.math.BigDecimal;

public record DeadlineOptionDto(
        DeadlineType code,
        String label,
        BigDecimal priceMultiplier,
        BigDecimal durationFactor
) {
    public static DeadlineOptionDto from(DeadlineType type) {
        return new DeadlineOptionDto(type, type.getLabel(), type.getPriceMultiplier(), type.getDurationFactor());
    }
}

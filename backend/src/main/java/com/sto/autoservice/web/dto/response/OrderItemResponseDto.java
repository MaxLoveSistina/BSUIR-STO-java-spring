package com.sto.autoservice.web.dto.response;

import com.sto.autoservice.core.entity.DeadlineType;
import com.sto.autoservice.core.entity.MaterialType;

import java.math.BigDecimal;

public record OrderItemResponseDto(
        Long serviceId,
        String serviceName,
        MaterialType materialType,
        String materialName,
        DeadlineType deadlineType,
        BigDecimal laborPrice,
        BigDecimal materialPrice,
        BigDecimal price,
        int durationDays
) {
}

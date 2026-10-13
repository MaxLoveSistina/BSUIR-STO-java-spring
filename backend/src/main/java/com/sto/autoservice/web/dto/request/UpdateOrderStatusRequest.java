package com.sto.autoservice.web.dto.request;

import com.sto.autoservice.core.entity.OrderStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateOrderStatusRequest(
        @NotNull(message = "Укажите новый статус")
        OrderStatus status
) {
}

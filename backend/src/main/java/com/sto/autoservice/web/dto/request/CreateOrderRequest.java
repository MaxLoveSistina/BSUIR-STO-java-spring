package com.sto.autoservice.web.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record CreateOrderRequest(
        @NotNull(message = "Выберите мастера")
        Long masterId,

        @NotBlank(message = "Укажите марку автомобиля")
        @Size(max = 50, message = "Марка не длиннее 50 символов")
        String carBrand,

        @NotBlank(message = "Укажите модель автомобиля")
        @Size(max = 50, message = "Модель не длиннее 50 символов")
        String carModel,

        @Size(max = 20, message = "Госномер не длиннее 20 символов")
        String plateNumber,

        @Size(max = 1000, message = "Комментарий не длиннее 1000 символов")
        String comment,

        @NotEmpty(message = "Выберите хотя бы одну услугу")
        List<@Valid @NotNull(message = "Позиция не может быть пустой") OrderItemRequest> items
) {
}

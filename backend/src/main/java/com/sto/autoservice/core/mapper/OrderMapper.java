package com.sto.autoservice.core.mapper;

import com.sto.autoservice.core.entity.OrderItem;
import com.sto.autoservice.core.entity.RepairOrder;
import com.sto.autoservice.core.repository.ClientSummary;
import com.sto.autoservice.web.dto.response.ClientResponseDto;
import com.sto.autoservice.web.dto.response.OrderItemResponseDto;
import com.sto.autoservice.web.dto.response.OrderResponseDto;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class OrderMapper {

    public OrderItemResponseDto toItemDto(OrderItem item) {
        return new OrderItemResponseDto(
                item.getService() != null ? item.getService().getId() : null,
                item.getServiceName(),
                item.getMaterialType(),
                item.getMaterialName(),
                item.getDeadlineType(),
                item.getLaborPrice(),
                item.getMaterialPrice(),
                item.getPrice(),
                item.getDurationDays()
        );
    }

    public OrderResponseDto toDto(RepairOrder order) {
        return new OrderResponseDto(
                order.getId(),
                order.getStatus(),
                order.getClient().getId(),
                order.getClient().getFullName(),
                order.getClient().getPhone(),
                order.getMaster().getId(),
                order.getMaster().getFullName(),
                order.getCarBrand(),
                order.getCarModel(),
                order.getPlateNumber(),
                order.getComment(),
                order.getItems().stream().map(this::toItemDto).toList(),
                order.getTotalPrice(),
                order.getTotalDurationDays(),
                order.getCreatedAt()
        );
    }

    public ClientResponseDto toClientDto(ClientSummary s) {
        return new ClientResponseDto(
                s.getId(),
                s.getFullName(),
                s.getPhone(),
                s.getEmail(),
                s.getOrdersCount() == null ? 0L : s.getOrdersCount(),
                s.getTotalSpent() == null ? BigDecimal.ZERO : s.getTotalSpent()
        );
    }
}

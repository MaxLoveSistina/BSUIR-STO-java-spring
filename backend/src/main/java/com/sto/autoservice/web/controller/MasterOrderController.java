package com.sto.autoservice.web.controller;

import com.sto.autoservice.core.entity.OrderStatus;
import com.sto.autoservice.core.service.OrderService;
import com.sto.autoservice.web.dto.request.UpdateOrderStatusRequest;
import com.sto.autoservice.web.dto.response.ClientResponseDto;
import com.sto.autoservice.web.dto.response.OrderResponseDto;
import com.sto.autoservice.web.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/master")
@RequiredArgsConstructor
@Tag(name = "Master: orders & clients", description = "Заказы и клиенты мастера (только ROLE_MASTER)")
public class MasterOrderController {

    private final OrderService orderService;

    @GetMapping("/orders")
    @Operation(summary = "Заказы мастера (опционально фильтр по статусу)")
    public List<OrderResponseDto> orders(@AuthenticationPrincipal UserPrincipal principal,
                                         @RequestParam(required = false) OrderStatus status) {
        return orderService.getMasterOrders(principal.getId(), status);
    }

    @GetMapping("/orders/{orderId}")
    @Operation(summary = "Заказ мастера по id")
    public OrderResponseDto order(@AuthenticationPrincipal UserPrincipal principal,
                                  @PathVariable Long orderId) {
        return orderService.getMasterOrder(principal.getId(), orderId);
    }

    @PatchMapping("/orders/{orderId}/status")
    @Operation(summary = "Сменить статус заказа (NEW → IN_PROGRESS → COMPLETED, либо CANCELLED)")
    public OrderResponseDto updateStatus(@AuthenticationPrincipal UserPrincipal principal,
                                         @PathVariable Long orderId,
                                         @Valid @RequestBody UpdateOrderStatusRequest request) {
        return orderService.updateStatusByMaster(principal.getId(), orderId, request.status());
    }

    @GetMapping("/clients")
    @Operation(summary = "Мои клиенты")
    public List<ClientResponseDto> clients(@AuthenticationPrincipal UserPrincipal principal) {
        return orderService.getMasterClients(principal.getId());
    }
}

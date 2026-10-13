package com.sto.autoservice.web.controller;

import com.sto.autoservice.core.service.OrderService;
import com.sto.autoservice.web.dto.request.CreateOrderRequest;
import com.sto.autoservice.web.dto.request.OrderCalculationRequest;
import com.sto.autoservice.web.dto.response.CalculationResponseDto;
import com.sto.autoservice.web.dto.response.OrderResponseDto;
import com.sto.autoservice.web.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/client/orders")
@RequiredArgsConstructor
@Tag(name = "Client: orders", description = "Заказы клиента (только ROLE_CLIENT)")
public class ClientOrderController {

    private final OrderService orderService;

    @PostMapping("/calculate")
    @Operation(summary = "Рассчитать общую стоимость и срок по выбранным услугам, материалам и срокам")
    public CalculationResponseDto calculate(@Valid @RequestBody OrderCalculationRequest request) {
        return orderService.calculate(request);
    }

    @PostMapping
    @Operation(summary = "Оформить заказ")
    public ResponseEntity<OrderResponseDto> create(@AuthenticationPrincipal UserPrincipal principal,
                                                   @Valid @RequestBody CreateOrderRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(orderService.create(principal.getId(), request));
    }

    @GetMapping
    @Operation(summary = "Мои заказы")
    public List<OrderResponseDto> list(@AuthenticationPrincipal UserPrincipal principal) {
        return orderService.getClientOrders(principal.getId());
    }

    @GetMapping("/{orderId}")
    @Operation(summary = "Мой заказ по id")
    public OrderResponseDto get(@AuthenticationPrincipal UserPrincipal principal, @PathVariable Long orderId) {
        return orderService.getClientOrder(principal.getId(), orderId);
    }

    @PatchMapping("/{orderId}/cancel")
    @Operation(summary = "Отменить заказ (пока он в статусе NEW)")
    public OrderResponseDto cancel(@AuthenticationPrincipal UserPrincipal principal, @PathVariable Long orderId) {
        return orderService.cancelByClient(principal.getId(), orderId);
    }
}

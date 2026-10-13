package com.sto.autoservice.core.service;

import com.sto.autoservice.core.entity.OrderStatus;
import com.sto.autoservice.web.dto.request.CreateOrderRequest;
import com.sto.autoservice.web.dto.request.OrderCalculationRequest;
import com.sto.autoservice.web.dto.response.CalculationResponseDto;
import com.sto.autoservice.web.dto.response.ClientResponseDto;
import com.sto.autoservice.web.dto.response.OrderResponseDto;

import java.util.List;

public interface OrderService {

    /** Предварительный расчёт общей стоимости и срока без создания заказа. */
    CalculationResponseDto calculate(OrderCalculationRequest request);

    OrderResponseDto create(Long clientId, CreateOrderRequest request);

    List<OrderResponseDto> getClientOrders(Long clientId);

    OrderResponseDto getClientOrder(Long clientId, Long orderId);

    /** Клиент может отменить заказ, пока мастер не взял его в работу. */
    OrderResponseDto cancelByClient(Long clientId, Long orderId);

    /** Заказы мастера; status == null — все статусы. */
    List<OrderResponseDto> getMasterOrders(Long masterId, OrderStatus status);

    OrderResponseDto getMasterOrder(Long masterId, Long orderId);

    OrderResponseDto updateStatusByMaster(Long masterId, Long orderId, OrderStatus newStatus);

    /** Клиенты мастера (те, кто делал у него заказы). */
    List<ClientResponseDto> getMasterClients(Long masterId);
}

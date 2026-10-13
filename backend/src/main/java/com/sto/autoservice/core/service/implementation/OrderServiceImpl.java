package com.sto.autoservice.core.service.implementation;

import com.sto.autoservice.core.entity.OrderItem;
import com.sto.autoservice.core.entity.OrderStatus;
import com.sto.autoservice.core.entity.RepairOrder;
import com.sto.autoservice.core.entity.RepairService;
import com.sto.autoservice.core.entity.Role;
import com.sto.autoservice.core.entity.User;
import com.sto.autoservice.core.exception.BadRequestException;
import com.sto.autoservice.core.exception.ResourceNotFoundException;
import com.sto.autoservice.core.mapper.OrderMapper;
import com.sto.autoservice.core.pricing.PriceCalculator;
import com.sto.autoservice.core.repository.RepairOrderRepository;
import com.sto.autoservice.core.repository.RepairServiceRepository;
import com.sto.autoservice.core.repository.UserRepository;
import com.sto.autoservice.core.service.OrderService;
import com.sto.autoservice.web.dto.request.CreateOrderRequest;
import com.sto.autoservice.web.dto.request.OrderCalculationRequest;
import com.sto.autoservice.web.dto.request.OrderItemRequest;
import com.sto.autoservice.web.dto.response.CalculationResponseDto;
import com.sto.autoservice.web.dto.response.ClientResponseDto;
import com.sto.autoservice.web.dto.response.OrderResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderServiceImpl implements OrderService {

    private final RepairOrderRepository orderRepository;
    private final RepairServiceRepository serviceRepository;
    private final UserRepository userRepository;
    private final PriceCalculator priceCalculator;
    private final OrderMapper orderMapper;

    // ------------------------------------------------------------------ расчёт и создание

    @Override
    @Transactional(readOnly = true)
    public CalculationResponseDto calculate(OrderCalculationRequest request) {
        User master = loadMaster(request.masterId());
        List<OrderItem> items = buildItems(master, request.items());

        return new CalculationResponseDto(
                master.getId(),
                items.stream().map(orderMapper::toItemDto).toList(),
                sumPrice(items),
                sumDays(items)
        );
    }

    @Override
    @Transactional
    public OrderResponseDto create(Long clientId, CreateOrderRequest request) {
        User client = userRepository.findById(clientId)
                .orElseThrow(() -> new ResourceNotFoundException("Клиент не найден"));
        User master = loadMaster(request.masterId());
        List<OrderItem> items = buildItems(master, request.items());

        RepairOrder order = RepairOrder.builder()
                .client(client)
                .master(master)
                .status(OrderStatus.NEW)
                .carBrand(request.carBrand().trim())
                .carModel(request.carModel().trim())
                .plateNumber(blankToNull(request.plateNumber()))
                .comment(blankToNull(request.comment()))
                .totalPrice(sumPrice(items))
                .totalDurationDays(sumDays(items))
                .build();
        items.forEach(order::addItem);

        RepairOrder saved = orderRepository.save(order);
        log.info("Order {} created: client={}, master={}, total={}",
                saved.getId(), clientId, master.getId(), saved.getTotalPrice());
        return orderMapper.toDto(saved);
    }

    // ------------------------------------------------------------------ клиент

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponseDto> getClientOrders(Long clientId) {
        return orderRepository.findByClientIdOrderByCreatedAtDesc(clientId).stream()
                .map(orderMapper::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponseDto getClientOrder(Long clientId, Long orderId) {
        return orderMapper.toDto(findClientOrder(clientId, orderId));
    }

    @Override
    @Transactional
    public OrderResponseDto cancelByClient(Long clientId, Long orderId) {
        RepairOrder order = findClientOrder(clientId, orderId);
        if (order.getStatus() != OrderStatus.NEW) {
            throw new BadRequestException("Отменить можно только заказ, который мастер ещё не взял в работу");
        }
        order.setStatus(OrderStatus.CANCELLED);
        return orderMapper.toDto(orderRepository.save(order));
    }

    // ------------------------------------------------------------------ мастер

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponseDto> getMasterOrders(Long masterId, OrderStatus status) {
        List<RepairOrder> orders = status == null
                ? orderRepository.findByMasterIdOrderByCreatedAtDesc(masterId)
                : orderRepository.findByMasterIdAndStatusOrderByCreatedAtDesc(masterId, status);
        return orders.stream().map(orderMapper::toDto).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponseDto getMasterOrder(Long masterId, Long orderId) {
        return orderMapper.toDto(findMasterOrder(masterId, orderId));
    }

    @Override
    @Transactional
    public OrderResponseDto updateStatusByMaster(Long masterId, Long orderId, OrderStatus newStatus) {
        RepairOrder order = findMasterOrder(masterId, orderId);
        if (!order.getStatus().canTransitionTo(newStatus)) {
            throw new BadRequestException(
                    "Нельзя перевести заказ из статуса " + order.getStatus() + " в " + newStatus);
        }
        order.setStatus(newStatus);
        return orderMapper.toDto(orderRepository.save(order));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClientResponseDto> getMasterClients(Long masterId) {
        return orderRepository.findClientsOfMaster(masterId, OrderStatus.CANCELLED).stream()
                .map(orderMapper::toClientDto)
                .toList();
    }

    // ------------------------------------------------------------------ внутреннее

    private User loadMaster(Long masterId) {
        return userRepository.findByIdAndRoleAndBlockedFalseAndDeletedFalse(masterId, Role.ROLE_MASTER)
                .orElseThrow(() -> new ResourceNotFoundException("Мастер не найден"));
    }

    private RepairOrder findClientOrder(Long clientId, Long orderId) {
        return orderRepository.findWithDetailsById(orderId)
                .filter(o -> o.getClient().getId().equals(clientId))
                .orElseThrow(() -> new ResourceNotFoundException("Заказ не найден"));
    }

    private RepairOrder findMasterOrder(Long masterId, Long orderId) {
        return orderRepository.findWithDetailsById(orderId)
                .filter(o -> o.getMaster().getId().equals(masterId))
                .orElseThrow(() -> new ResourceNotFoundException("Заказ не найден"));
    }

    /**
     * Собирает позиции заказа: проверяет, что все услуги принадлежат выбранному мастеру и активны,
     * и считает цену каждой позиции по выбранным материалам и сроку.
     */
    private List<OrderItem> buildItems(User master, List<OrderItemRequest> requested) {
        Set<Long> ids = new HashSet<>();
        for (OrderItemRequest r : requested) {
            if (!ids.add(r.serviceId())) {
                throw new BadRequestException("Услуга выбрана несколько раз (id=" + r.serviceId() + ")");
            }
        }

        Map<Long, RepairService> available = serviceRepository
                .findByIdInAndMasterIdAndActiveTrueAndDeletedFalse(ids, master.getId()).stream()
                .collect(Collectors.toMap(RepairService::getId, Function.identity()));

        List<OrderItem> items = new ArrayList<>();
        for (OrderItemRequest r : requested) {
            RepairService service = available.get(r.serviceId());
            if (service == null) {
                throw new BadRequestException(
                        "Услуга id=" + r.serviceId() + " недоступна у выбранного мастера");
            }

            PriceCalculator.ItemPrice price = priceCalculator.calculate(
                    service.getLaborPrice(),
                    service.getDurationDays(),
                    service.materialPriceFor(r.materialType()),
                    r.deadlineType());

            items.add(OrderItem.builder()
                    .service(service)
                    .serviceName(service.getName())
                    .materialType(r.materialType())
                    .materialName(service.materialNameFor(r.materialType()))
                    .deadlineType(r.deadlineType())
                    .laborPrice(price.laborPrice())
                    .materialPrice(price.materialPrice())
                    .price(price.totalPrice())
                    .durationDays(price.durationDays())
                    .build());
        }
        return items;
    }

    private BigDecimal sumPrice(List<OrderItem> items) {
        return items.stream().map(OrderItem::getPrice).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private int sumDays(List<OrderItem> items) {
        return items.stream().mapToInt(OrderItem::getDurationDays).sum();
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}

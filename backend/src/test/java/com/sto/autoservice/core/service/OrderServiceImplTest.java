package com.sto.autoservice.core.service;

import com.sto.autoservice.core.entity.DeadlineType;
import com.sto.autoservice.core.entity.MaterialType;
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
import com.sto.autoservice.core.service.implementation.OrderServiceImpl;
import com.sto.autoservice.web.dto.request.OrderCalculationRequest;
import com.sto.autoservice.web.dto.request.OrderItemRequest;
import com.sto.autoservice.web.dto.response.CalculationResponseDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @Mock
    private RepairOrderRepository orderRepository;
    @Mock
    private RepairServiceRepository serviceRepository;
    @Mock
    private UserRepository userRepository;

    private OrderServiceImpl orderService;

    private User master;
    private RepairService oilChange;
    private RepairService brakes;

    @BeforeEach
    void setUp() {
        orderService = new OrderServiceImpl(orderRepository, serviceRepository, userRepository,
                new PriceCalculator(), new OrderMapper());

        master = User.builder().id(10L).username("m").fullName("Мастер").role(Role.ROLE_MASTER).build();

        // работа 1500, 1 день; дешёвые материалы 2200, дорогие 5200
        oilChange = service(1L, "Замена масла", "1500", 1, "2200", "5200");
        // работа 2500, 2 дня; дешёвые материалы 1800, дорогие 5500
        brakes = service(2L, "Замена колодок", "2500", 2, "1800", "5500");
    }

    private RepairService service(Long id, String name, String labor, int days, String eco, String premium) {
        return RepairService.builder()
                .id(id).master(master).name(name)
                .laborPrice(new BigDecimal(labor)).durationDays(days)
                .economyMaterialName("Эконом").economyMaterialPrice(new BigDecimal(eco))
                .premiumMaterialName("Премиум").premiumMaterialPrice(new BigDecimal(premium))
                .build();
    }

    @Test
    @DisplayName("Общая стоимость нескольких услуг с разными материалами и сроками")
    void calculateSumsAllItems() {
        when(userRepository.findByIdAndRoleAndBlockedFalseAndDeletedFalse(10L, Role.ROLE_MASTER))
                .thenReturn(Optional.of(master));
        when(serviceRepository.findByIdInAndMasterIdAndActiveTrueAndDeletedFalse(anyCollection(), eq(10L)))
                .thenReturn(List.of(oilChange, brakes));

        var request = new OrderCalculationRequest(10L, List.of(
                // 1500 + 2200 = 3700, 1 день
                new OrderItemRequest(1L, MaterialType.ECONOMY, DeadlineType.STANDARD),
                // 2500 × 1.3 = 3250 + 5500 = 8750, 2 × 0.5 = 1 день
                new OrderItemRequest(2L, MaterialType.PREMIUM, DeadlineType.EXPRESS)
        ));

        CalculationResponseDto result = orderService.calculate(request);

        assertThat(result.items()).hasSize(2);
        assertThat(result.items().get(0).price()).isEqualByComparingTo("3700.00");
        assertThat(result.items().get(1).price()).isEqualByComparingTo("8750.00");
        assertThat(result.totalPrice()).isEqualByComparingTo("12450.00");
        assertThat(result.totalDurationDays()).isEqualTo(2);
    }

    @Test
    @DisplayName("Услуга, которой нет у выбранного мастера, отклоняется")
    void calculateRejectsForeignService() {
        when(userRepository.findByIdAndRoleAndBlockedFalseAndDeletedFalse(10L, Role.ROLE_MASTER))
                .thenReturn(Optional.of(master));
        when(serviceRepository.findByIdInAndMasterIdAndActiveTrueAndDeletedFalse(anyCollection(), eq(10L)))
                .thenReturn(List.of(oilChange));

        var request = new OrderCalculationRequest(10L, List.of(
                new OrderItemRequest(1L, MaterialType.ECONOMY, DeadlineType.STANDARD),
                new OrderItemRequest(99L, MaterialType.ECONOMY, DeadlineType.STANDARD)
        ));

        assertThatThrownBy(() -> orderService.calculate(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("99");
    }

    @Test
    @DisplayName("Одна и та же услуга дважды — ошибка")
    void calculateRejectsDuplicates() {
        when(userRepository.findByIdAndRoleAndBlockedFalseAndDeletedFalse(10L, Role.ROLE_MASTER))
                .thenReturn(Optional.of(master));

        var request = new OrderCalculationRequest(10L, List.of(
                new OrderItemRequest(1L, MaterialType.ECONOMY, DeadlineType.STANDARD),
                new OrderItemRequest(1L, MaterialType.PREMIUM, DeadlineType.URGENT)
        ));

        assertThatThrownBy(() -> orderService.calculate(request))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    @DisplayName("Несуществующий мастер — 404")
    void calculateUnknownMaster() {
        when(userRepository.findByIdAndRoleAndBlockedFalseAndDeletedFalse(5L, Role.ROLE_MASTER))
                .thenReturn(Optional.empty());

        var request = new OrderCalculationRequest(5L, List.of(
                new OrderItemRequest(1L, MaterialType.ECONOMY, DeadlineType.STANDARD)));

        assertThatThrownBy(() -> orderService.calculate(request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("Мастер не может перевести завершённый заказ в другой статус")
    void masterCannotReopenCompletedOrder() {
        RepairOrder order = orderOf(master, OrderStatus.COMPLETED);
        when(orderRepository.findWithDetailsById(7L)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.updateStatusByMaster(10L, 7L, OrderStatus.IN_PROGRESS))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    @DisplayName("Мастер не видит чужие заказы")
    void masterCannotTouchForeignOrder() {
        User otherMaster = User.builder().id(11L).role(Role.ROLE_MASTER).build();
        RepairOrder order = orderOf(otherMaster, OrderStatus.NEW);
        when(orderRepository.findWithDetailsById(7L)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.updateStatusByMaster(10L, 7L, OrderStatus.IN_PROGRESS))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("Клиент отменяет заказ в статусе NEW")
    void clientCancelsNewOrder() {
        RepairOrder order = orderOf(master, OrderStatus.NEW);
        when(orderRepository.findWithDetailsById(7L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(RepairOrder.class))).thenAnswer(inv -> inv.getArgument(0));

        var result = orderService.cancelByClient(20L, 7L);

        assertThat(result.status()).isEqualTo(OrderStatus.CANCELLED);
        verify(orderRepository).save(order);
    }

    @Test
    @DisplayName("Клиент не может отменить заказ, который уже в работе")
    void clientCannotCancelInProgressOrder() {
        RepairOrder order = orderOf(master, OrderStatus.IN_PROGRESS);
        when(orderRepository.findWithDetailsById(7L)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.cancelByClient(20L, 7L))
                .isInstanceOf(BadRequestException.class);
    }

    private RepairOrder orderOf(User orderMaster, OrderStatus status) {
        User client = User.builder().id(20L).fullName("Клиент").role(Role.ROLE_CLIENT).build();
        return RepairOrder.builder()
                .id(7L).client(client).master(orderMaster).status(status)
                .totalPrice(BigDecimal.TEN).totalDurationDays(1)
                .build();
    }
}

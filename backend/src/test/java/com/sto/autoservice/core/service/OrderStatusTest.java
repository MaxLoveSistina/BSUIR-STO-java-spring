package com.sto.autoservice.core.service;

import com.sto.autoservice.core.entity.OrderStatus;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class OrderStatusTest {

    @Test
    void allowedTransitions() {
        assertThat(OrderStatus.NEW.canTransitionTo(OrderStatus.IN_PROGRESS)).isTrue();
        assertThat(OrderStatus.NEW.canTransitionTo(OrderStatus.CANCELLED)).isTrue();
        assertThat(OrderStatus.IN_PROGRESS.canTransitionTo(OrderStatus.COMPLETED)).isTrue();
        assertThat(OrderStatus.IN_PROGRESS.canTransitionTo(OrderStatus.CANCELLED)).isTrue();
    }

    @Test
    void forbiddenTransitions() {
        assertThat(OrderStatus.NEW.canTransitionTo(OrderStatus.COMPLETED)).isFalse();
        assertThat(OrderStatus.IN_PROGRESS.canTransitionTo(OrderStatus.NEW)).isFalse();
        assertThat(OrderStatus.COMPLETED.canTransitionTo(OrderStatus.CANCELLED)).isFalse();
        assertThat(OrderStatus.CANCELLED.canTransitionTo(OrderStatus.NEW)).isFalse();
    }
}

package com.sto.autoservice.core.repository;

import java.math.BigDecimal;

/**
 * Проекция «клиент мастера» со сводкой по заказам.
 */
public interface ClientSummary {

    Long getId();

    String getFullName();

    String getPhone();

    String getEmail();

    Long getOrdersCount();

    BigDecimal getTotalSpent();
}

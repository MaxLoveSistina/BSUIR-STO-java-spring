package com.sto.autoservice.core.repository;

import com.sto.autoservice.core.entity.OrderStatus;
import com.sto.autoservice.core.entity.RepairOrder;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RepairOrderRepository extends JpaRepository<RepairOrder, Long> {

    @EntityGraph(attributePaths = {"client", "master", "items"})
    List<RepairOrder> findByClientIdOrderByCreatedAtDesc(Long clientId);

    @EntityGraph(attributePaths = {"client", "master", "items"})
    List<RepairOrder> findByMasterIdOrderByCreatedAtDesc(Long masterId);

    @EntityGraph(attributePaths = {"client", "master", "items"})
    List<RepairOrder> findByMasterIdAndStatusOrderByCreatedAtDesc(Long masterId, OrderStatus status);

    @EntityGraph(attributePaths = {"client", "master", "items"})
    Optional<RepairOrder> findWithDetailsById(Long id);

    /**
     * Клиенты конкретного мастера: все, кто оформлял у него заказы (кроме статуса {@code excluded}),
     * с количеством заказов и потраченной суммой.
     */
    @Query("""
            SELECT c.id AS id, c.fullName AS fullName, c.phone AS phone, c.email AS email,
                   COUNT(o) AS ordersCount, SUM(o.totalPrice) AS totalSpent
            FROM RepairOrder o JOIN o.client c
            WHERE o.master.id = :masterId AND o.status <> :excluded
            GROUP BY c.id, c.fullName, c.phone, c.email
            ORDER BY c.fullName
            """)
    List<ClientSummary> findClientsOfMaster(@Param("masterId") Long masterId,
                                            @Param("excluded") OrderStatus excluded);
}

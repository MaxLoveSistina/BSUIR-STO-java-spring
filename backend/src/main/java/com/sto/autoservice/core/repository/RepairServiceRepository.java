package com.sto.autoservice.core.repository;

import com.sto.autoservice.core.entity.RepairService;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface RepairServiceRepository extends JpaRepository<RepairService, Long> {

    /** Все не удалённые услуги мастера (для кабинета мастера). */
    List<RepairService> findByMasterIdAndDeletedFalseOrderByNameAsc(Long masterId);

    /** Услуги, доступные клиентам для заказа. */
    List<RepairService> findByMasterIdAndActiveTrueAndDeletedFalseOrderByNameAsc(Long masterId);

    Optional<RepairService> findByIdAndMasterIdAndDeletedFalse(Long id, Long masterId);

    /** Выбранные клиентом услуги — только активные и только данного мастера. */
    List<RepairService> findByIdInAndMasterIdAndActiveTrueAndDeletedFalse(Collection<Long> ids, Long masterId);
}

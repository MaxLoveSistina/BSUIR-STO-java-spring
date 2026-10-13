package com.sto.autoservice.core.service;

import com.sto.autoservice.web.dto.request.ServiceRequest;
import com.sto.autoservice.web.dto.response.ServiceResponseDto;

import java.util.List;

/**
 * Каталог услуг: у каждого мастера — свой набор.
 */
public interface CatalogService {

    /** Все (не удалённые) услуги мастера — для его кабинета. */
    List<ServiceResponseDto> getMasterServices(Long masterId);

    /** Услуги мастера, доступные клиентам для заказа. */
    List<ServiceResponseDto> getPublicServices(Long masterId);

    ServiceResponseDto create(Long masterId, ServiceRequest request);

    ServiceResponseDto update(Long masterId, Long serviceId, ServiceRequest request);

    void delete(Long masterId, Long serviceId);
}

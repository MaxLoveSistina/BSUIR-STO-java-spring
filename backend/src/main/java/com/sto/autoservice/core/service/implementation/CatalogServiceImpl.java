package com.sto.autoservice.core.service.implementation;

import com.sto.autoservice.core.entity.RepairService;
import com.sto.autoservice.core.entity.Role;
import com.sto.autoservice.core.entity.User;
import com.sto.autoservice.core.exception.BadRequestException;
import com.sto.autoservice.core.exception.ResourceNotFoundException;
import com.sto.autoservice.core.mapper.ServiceMapper;
import com.sto.autoservice.core.repository.RepairServiceRepository;
import com.sto.autoservice.core.repository.UserRepository;
import com.sto.autoservice.core.service.CatalogService;
import com.sto.autoservice.web.dto.request.ServiceRequest;
import com.sto.autoservice.web.dto.response.ServiceResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CatalogServiceImpl implements CatalogService {

    private final RepairServiceRepository serviceRepository;
    private final UserRepository userRepository;
    private final ServiceMapper serviceMapper;

    @Override
    @Transactional(readOnly = true)
    public List<ServiceResponseDto> getMasterServices(Long masterId) {
        return serviceRepository.findByMasterIdAndDeletedFalseOrderByNameAsc(masterId).stream()
                .map(serviceMapper::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ServiceResponseDto> getPublicServices(Long masterId) {
        userRepository.findByIdAndRoleAndBlockedFalseAndDeletedFalse(masterId, Role.ROLE_MASTER)
                .orElseThrow(() -> new ResourceNotFoundException("Мастер не найден"));
        return serviceRepository.findByMasterIdAndActiveTrueAndDeletedFalseOrderByNameAsc(masterId).stream()
                .map(serviceMapper::toDto)
                .toList();
    }

    @Override
    @Transactional
    public ServiceResponseDto create(Long masterId, ServiceRequest request) {
        validate(request);
        User master = userRepository.findById(masterId)
                .orElseThrow(() -> new ResourceNotFoundException("Мастер не найден"));

        RepairService service = RepairService.builder().master(master).build();
        apply(service, request);
        return serviceMapper.toDto(serviceRepository.save(service));
    }

    @Override
    @Transactional
    public ServiceResponseDto update(Long masterId, Long serviceId, ServiceRequest request) {
        validate(request);
        RepairService service = findOwned(masterId, serviceId);
        apply(service, request);
        return serviceMapper.toDto(serviceRepository.save(service));
    }

    @Override
    @Transactional
    public void delete(Long masterId, Long serviceId) {
        RepairService service = findOwned(masterId, serviceId);
        // Мягкое удаление: старые заказы продолжают ссылаться на услугу
        service.setDeleted(true);
        service.setActive(false);
        serviceRepository.save(service);
    }

    private RepairService findOwned(Long masterId, Long serviceId) {
        return serviceRepository.findByIdAndMasterIdAndDeletedFalse(serviceId, masterId)
                .orElseThrow(() -> new ResourceNotFoundException("Услуга не найдена"));
    }

    private void validate(ServiceRequest r) {
        if (r.premiumMaterialPrice().compareTo(r.economyMaterialPrice()) < 0) {
            throw new BadRequestException("Дорогие материалы не могут стоить меньше дешёвых");
        }
    }

    private void apply(RepairService s, ServiceRequest r) {
        s.setName(r.name().trim());
        s.setDescription(r.description() == null || r.description().isBlank() ? null : r.description().trim());
        s.setLaborPrice(r.laborPrice());
        s.setDurationDays(r.durationDays());
        s.setEconomyMaterialName(r.economyMaterialName().trim());
        s.setEconomyMaterialPrice(r.economyMaterialPrice());
        s.setPremiumMaterialName(r.premiumMaterialName().trim());
        s.setPremiumMaterialPrice(r.premiumMaterialPrice());
        s.setActive(r.active() == null || r.active());
    }
}

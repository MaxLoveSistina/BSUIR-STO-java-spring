package com.sto.autoservice.core.mapper;

import com.sto.autoservice.core.entity.RepairService;
import com.sto.autoservice.web.dto.response.ServiceResponseDto;
import org.springframework.stereotype.Component;

@Component
public class ServiceMapper {

    public ServiceResponseDto toDto(RepairService s) {
        return new ServiceResponseDto(
                s.getId(),
                s.getMaster().getId(),
                s.getMaster().getFullName(),
                s.getName(),
                s.getDescription(),
                s.getLaborPrice(),
                s.getDurationDays(),
                s.getEconomyMaterialName(),
                s.getEconomyMaterialPrice(),
                s.getPremiumMaterialName(),
                s.getPremiumMaterialPrice(),
                s.isActive()
        );
    }
}

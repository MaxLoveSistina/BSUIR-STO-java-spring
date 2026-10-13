package com.sto.autoservice.web.controller;

import com.sto.autoservice.core.service.CatalogService;
import com.sto.autoservice.core.service.UserService;
import com.sto.autoservice.web.dto.response.MasterResponseDto;
import com.sto.autoservice.web.dto.response.ServiceResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/masters")
@RequiredArgsConstructor
@Tag(name = "Masters", description = "Публичный каталог мастеров и их услуг")
public class MasterDirectoryController {

    private final UserService userService;
    private final CatalogService catalogService;

    @GetMapping
    @Operation(summary = "Список мастеров")
    public List<MasterResponseDto> masters() {
        return userService.getMasters();
    }

    @GetMapping("/{masterId}")
    @Operation(summary = "Мастер по id")
    public MasterResponseDto master(@PathVariable Long masterId) {
        return userService.getMaster(masterId);
    }

    @GetMapping("/{masterId}/services")
    @Operation(summary = "Услуги мастера, доступные для заказа")
    public List<ServiceResponseDto> services(@PathVariable Long masterId) {
        return catalogService.getPublicServices(masterId);
    }
}

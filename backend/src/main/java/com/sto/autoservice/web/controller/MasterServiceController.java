package com.sto.autoservice.web.controller;

import com.sto.autoservice.core.service.CatalogService;
import com.sto.autoservice.web.dto.request.ServiceRequest;
import com.sto.autoservice.web.dto.response.ServiceResponseDto;
import com.sto.autoservice.web.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/master/services")
@RequiredArgsConstructor
@Tag(name = "Master: services", description = "Каталог услуг мастера (только ROLE_MASTER)")
public class MasterServiceController {

    private final CatalogService catalogService;

    @GetMapping
    @Operation(summary = "Мои услуги")
    public List<ServiceResponseDto> list(@AuthenticationPrincipal UserPrincipal principal) {
        return catalogService.getMasterServices(principal.getId());
    }

    @PostMapping
    @Operation(summary = "Добавить услугу")
    public ResponseEntity<ServiceResponseDto> create(@AuthenticationPrincipal UserPrincipal principal,
                                                     @Valid @RequestBody ServiceRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(catalogService.create(principal.getId(), request));
    }

    @PutMapping("/{serviceId}")
    @Operation(summary = "Изменить услугу")
    public ServiceResponseDto update(@AuthenticationPrincipal UserPrincipal principal,
                                     @PathVariable Long serviceId,
                                     @Valid @RequestBody ServiceRequest request) {
        return catalogService.update(principal.getId(), serviceId, request);
    }

    @DeleteMapping("/{serviceId}")
    @Operation(summary = "Удалить услугу (мягкое удаление)")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal UserPrincipal principal,
                                       @PathVariable Long serviceId) {
        catalogService.delete(principal.getId(), serviceId);
        return ResponseEntity.noContent().build();
    }
}

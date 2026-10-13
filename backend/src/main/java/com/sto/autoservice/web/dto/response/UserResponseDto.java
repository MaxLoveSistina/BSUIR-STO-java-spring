package com.sto.autoservice.web.dto.response;

import com.sto.autoservice.core.entity.Role;

import java.time.OffsetDateTime;

public record UserResponseDto(
        Long id,
        String username,
        String email,
        String fullName,
        String phone,
        Role role,
        OffsetDateTime createdAt
) {
}

package com.sto.autoservice.web.dto.response;

public record AuthResponseDto(
        String token,
        String tokenType,
        UserResponseDto user
) {
    public AuthResponseDto(String token, UserResponseDto user) {
        this(token, "Bearer", user);
    }
}

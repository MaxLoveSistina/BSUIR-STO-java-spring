package com.sto.autoservice.web.dto.request;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank(message = "Укажите логин или email")
        String usernameOrEmail,

        @NotBlank(message = "Укажите пароль")
        String password
) {
}

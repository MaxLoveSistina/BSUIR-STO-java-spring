package com.sto.autoservice.web.dto.request;

import com.sto.autoservice.core.entity.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank(message = "Имя пользователя не может быть пустым")
        @Size(min = 3, max = 50, message = "Имя пользователя должно быть от 3 до 50 символов")
        String username,

        @NotBlank(message = "Email не может быть пустым")
        @Email(message = "Некорректный формат email")
        @Size(max = 100, message = "Email не длиннее 100 символов")
        String email,

        @NotBlank(message = "Пароль не может быть пустым")
        @Size(min = 6, max = 100, message = "Пароль должен быть длиной от 6 символов")
        String password,

        @NotBlank(message = "Укажите имя")
        @Size(max = 100, message = "Имя не длиннее 100 символов")
        String fullName,

        @Size(max = 30, message = "Телефон не длиннее 30 символов")
        String phone,

        @NotNull(message = "Укажите роль: ROLE_CLIENT или ROLE_MASTER")
        Role role
) {
}

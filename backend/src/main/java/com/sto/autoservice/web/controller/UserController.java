package com.sto.autoservice.web.controller;

import com.sto.autoservice.core.service.UserService;
import com.sto.autoservice.web.dto.response.UserResponseDto;
import com.sto.autoservice.web.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "Текущий пользователь")
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    @Operation(summary = "Профиль текущего пользователя")
    public UserResponseDto me(@AuthenticationPrincipal UserPrincipal principal) {
        return userService.getById(principal.getId());
    }
}

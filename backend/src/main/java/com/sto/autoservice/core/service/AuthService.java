package com.sto.autoservice.core.service;

import com.sto.autoservice.core.entity.Role;
import com.sto.autoservice.core.entity.User;
import com.sto.autoservice.core.exception.BadRequestException;
import com.sto.autoservice.core.exception.UserAlreadyExistsException;
import com.sto.autoservice.core.mapper.UserMapper;
import com.sto.autoservice.core.repository.UserRepository;
import com.sto.autoservice.web.dto.request.LoginRequest;
import com.sto.autoservice.web.dto.request.RegisterRequest;
import com.sto.autoservice.web.dto.response.AuthResponseDto;
import com.sto.autoservice.web.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
    private final AuthenticationManager authenticationManager;

    @Transactional
    public AuthResponseDto register(RegisterRequest request) {
        if (request.role() != Role.ROLE_CLIENT && request.role() != Role.ROLE_MASTER) {
            throw new BadRequestException("Допустимые роли при регистрации: ROLE_CLIENT, ROLE_MASTER");
        }
        if (userRepository.existsByUsername(request.username())) {
            throw new UserAlreadyExistsException("Username already in use");
        }
        if (userRepository.existsByEmail(request.email())) {
            throw new UserAlreadyExistsException("Email already in use");
        }

        User user = User.builder()
                .username(request.username().trim())
                .email(request.email().trim())
                .password(passwordEncoder.encode(request.password()))
                .fullName(request.fullName().trim())
                .phone(request.phone() == null || request.phone().isBlank() ? null : request.phone().trim())
                .role(request.role())
                .build();

        User saved = userRepository.save(user);
        String token = tokenProvider.generateToken(saved);
        return new AuthResponseDto(token, userMapper.toResponseDto(saved));
    }

    @Transactional(readOnly = true)
    public AuthResponseDto login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.usernameOrEmail(), request.password())
        );

        User user = userRepository.findByUsernameOrEmail(request.usernameOrEmail()).orElseThrow();
        String token = tokenProvider.generateToken(user);
        return new AuthResponseDto(token, userMapper.toResponseDto(user));
    }
}

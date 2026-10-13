package com.sto.autoservice.core.service;

import com.sto.autoservice.core.entity.Role;
import com.sto.autoservice.core.exception.ResourceNotFoundException;
import com.sto.autoservice.core.mapper.UserMapper;
import com.sto.autoservice.core.repository.UserRepository;
import com.sto.autoservice.web.dto.response.MasterResponseDto;
import com.sto.autoservice.web.dto.response.UserResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public UserResponseDto getById(Long id) {
        return userRepository.findById(id)
                .map(userMapper::toResponseDto)
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь не найден"));
    }

    public List<MasterResponseDto> getMasters() {
        return userRepository.findByRoleAndBlockedFalseAndDeletedFalseOrderByFullNameAsc(Role.ROLE_MASTER)
                .stream()
                .map(userMapper::toMasterDto)
                .toList();
    }

    public MasterResponseDto getMaster(Long id) {
        return userRepository.findByIdAndRoleAndBlockedFalseAndDeletedFalse(id, Role.ROLE_MASTER)
                .map(userMapper::toMasterDto)
                .orElseThrow(() -> new ResourceNotFoundException("Мастер не найден"));
    }
}

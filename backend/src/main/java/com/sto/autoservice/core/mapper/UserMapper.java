package com.sto.autoservice.core.mapper;

import com.sto.autoservice.core.entity.User;
import com.sto.autoservice.web.dto.response.MasterResponseDto;
import com.sto.autoservice.web.dto.response.UserResponseDto;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserResponseDto toResponseDto(User user) {
        return new UserResponseDto(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getFullName(),
                user.getPhone(),
                user.getRole(),
                user.getCreatedAt()
        );
    }

    public MasterResponseDto toMasterDto(User user) {
        return new MasterResponseDto(user.getId(), user.getFullName(), user.getPhone());
    }
}

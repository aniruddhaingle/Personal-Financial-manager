package com.syfe.personalfinance.mapper;

import com.syfe.personalfinance.dto.AuthDto;
import com.syfe.personalfinance.entity.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public AuthDto.UserResponse toResponse(User user) {
        if (user == null) {
            return null;
        }
        return AuthDto.UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .fullName(user.getFullName())
                .phoneNumber(user.getPhoneNumber())
                .build();
    }

    public User toEntity(AuthDto.RegisterRequest request) {
        if (request == null) {
            return null;
        }
        return User.builder()
                .username(request.getUsername())
                .fullName(request.getFullName())
                .phoneNumber(request.getPhoneNumber())
                .build();
    }
}

package com.syfe.personalfinance.service;

import com.syfe.personalfinance.dto.AuthDto;
import com.syfe.personalfinance.entity.User;
import org.springframework.security.core.userdetails.UserDetailsService;

public interface UserService extends UserDetailsService {
    AuthDto.UserResponse register(AuthDto.RegisterRequest request);
    User getAuthenticatedUserEntity();
}

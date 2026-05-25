package com.syfe.personalfinance.service.impl;

import com.syfe.personalfinance.dto.AuthDto;
import com.syfe.personalfinance.entity.User;
import com.syfe.personalfinance.exception.ConflictException;
import com.syfe.personalfinance.exception.ResourceNotFoundException;
import com.syfe.personalfinance.mapper.UserMapper;
import com.syfe.personalfinance.repository.UserRepository;
import com.syfe.personalfinance.service.UserService;
import com.syfe.personalfinance.util.SecurityUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

@Service
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    // Constructor injection only
    public UserServiceImpl(UserRepository userRepository, UserMapper userMapper, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public AuthDto.UserResponse register(AuthDto.RegisterRequest request) {
        log.info("Processing registration request for username: {}", request.getUsername());

        if (userRepository.existsByUsername(request.getUsername())) {
            log.warn("Registration failed: Username {} already exists", request.getUsername());
            throw new ConflictException("Username/Email already exists");
        }

        User user = userMapper.toEntity(request);
        user.setPassword(passwordEncoder.encode(request.getPassword())); // Enforce BCrypt password hashing

        User savedUser = userRepository.save(user);
        log.info("User registered successfully with ID: {}", savedUser.getId());

        return userMapper.toResponse(savedUser);
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.debug("Loading user security context details for username: {}", username);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.warn("User auth retrieval failed for username: {}", username);
                    return new UsernameNotFoundException("User not found with username: " + username);
                });

        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );
    }

    @Override
    @Transactional(readOnly = true)
    public User getAuthenticatedUserEntity() {
        String username = SecurityUtils.getCurrentUsername();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.error("Authenticated user context username: {} not found in database", username);
                    return new ResourceNotFoundException("Authenticated user not found");
                });
    }
}

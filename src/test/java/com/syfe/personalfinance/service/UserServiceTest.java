package com.syfe.personalfinance.service;

import com.syfe.personalfinance.dto.AuthDto;
import com.syfe.personalfinance.dto.AuthDto.UserResponse;
import com.syfe.personalfinance.entity.User;
import com.syfe.personalfinance.exception.ConflictException;
import com.syfe.personalfinance.mapper.UserMapper;
import com.syfe.personalfinance.repository.UserRepository;
import com.syfe.personalfinance.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserServiceImpl(userRepository, userMapper, passwordEncoder);
    }

    @Test
    void register_Success() {
        AuthDto.RegisterRequest request = AuthDto.RegisterRequest.builder()
                .username("test@syfe.com")
                .password("SecurePassword123!")
                .fullName("Test User")
                .build();

        User userEntity = User.builder()
                .username("test@syfe.com")
                .password("encodedPassword")
                .fullName("Test User")
                .build();

        UserResponse expectedResponse = UserResponse.builder()
                .id(1L)
                .username("test@syfe.com")
                .fullName("Test User")
                .build();

        when(userRepository.existsByUsername(request.getUsername())).thenReturn(false);
        when(userMapper.toEntity(request)).thenReturn(userEntity);
        when(passwordEncoder.encode(request.getPassword())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(userEntity);
        when(userMapper.toResponse(any(User.class))).thenReturn(expectedResponse);

        UserResponse actualResponse = userService.register(request);

        assertNotNull(actualResponse);
        assertEquals("test@syfe.com", actualResponse.getUsername());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void register_ConflictingUsername_ThrowsException() {
        AuthDto.RegisterRequest request = AuthDto.RegisterRequest.builder()
                .username("duplicate@syfe.com")
                .password("SecurePassword123!")
                .fullName("Duplicate User")
                .build();

        when(userRepository.existsByUsername(request.getUsername())).thenReturn(true);

        assertThrows(ConflictException.class, () -> userService.register(request));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void loadUserByUsername_Success() {
        User userEntity = User.builder()
                .username("user@syfe.com")
                .password("encoded_pass")
                .build();

        when(userRepository.findByUsername("user@syfe.com")).thenReturn(Optional.of(userEntity));

        UserDetails userDetails = userService.loadUserByUsername("user@syfe.com");

        assertNotNull(userDetails);
        assertEquals("user@syfe.com", userDetails.getUsername());
        assertEquals("encoded_pass", userDetails.getPassword());
    }

    @Test
    void loadUserByUsername_NotFound_ThrowsException() {
        when(userRepository.findByUsername("nonexistent@syfe.com")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> userService.loadUserByUsername("nonexistent@syfe.com"));
    }
}

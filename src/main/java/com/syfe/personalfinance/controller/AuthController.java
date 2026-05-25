package com.syfe.personalfinance.controller;

import com.syfe.personalfinance.dto.AuthDto;
import com.syfe.personalfinance.dto.AuthDto.UserResponse;
import com.syfe.personalfinance.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;

    public AuthController(UserService userService, AuthenticationManager authenticationManager) {
        this.userService = userService;
        this.authenticationManager = authenticationManager;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthDto.RegisterResponse> register(@Valid @RequestBody AuthDto.RegisterRequest request) {
        UserResponse responseData = userService.register(request);
        AuthDto.RegisterResponse response = AuthDto.RegisterResponse.builder()
                .message("User registered successfully")
                .userId(responseData.getId())
                .build();
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthDto.SimpleMessageResponse> login(@Valid @RequestBody AuthDto.LoginRequest request, HttpServletRequest servletRequest) {
        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                request.getUsername(), request.getPassword());

        Authentication authentication = authenticationManager.authenticate(authToken);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        HttpSession session = servletRequest.getSession(true);
        session.setAttribute("SPRING_SECURITY_CONTEXT", SecurityContextHolder.getContext());

        AuthDto.SimpleMessageResponse response = AuthDto.SimpleMessageResponse.builder()
                .message("Login successful")
                .build();

        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<AuthDto.SimpleMessageResponse> logout(HttpServletRequest servletRequest) {
        HttpSession session = servletRequest.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        SecurityContextHolder.clearContext();

        AuthDto.SimpleMessageResponse response = AuthDto.SimpleMessageResponse.builder()
                .message("Logout successful")
                .build();

        return ResponseEntity.ok(response);
    }
}

package com.syfe.personalfinance.controller

import com.syfe.personalfinance.dto.AuthDto
import com.syfe.personalfinance.service.UserService
import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * REST controller managing user authentication lifecycle: registration, login session establishment, and logout.
 */
@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val userService: UserService,
    private val authenticationManager: AuthenticationManager
) {

    /**
     * Registers a new user account with validated credentials.
     */
    @PostMapping("/register")
    fun register(@Valid @RequestBody request: AuthDto.RegisterRequest): ResponseEntity<AuthDto.RegisterResponse> {
        val responseData = userService.register(request)
        val response = AuthDto.RegisterResponse(
            message = "User registered successfully",
            userId = responseData.id
        )
        return ResponseEntity(response, HttpStatus.CREATED)
    }

    /**
     * Authenticates user credentials and establishes an HTTP session (JSESSIONID cookie).
     */
    @PostMapping("/login")
    fun login(
        @Valid @RequestBody request: AuthDto.LoginRequest,
        servletRequest: HttpServletRequest
    ): ResponseEntity<AuthDto.SimpleMessageResponse> {
        val authToken = UsernamePasswordAuthenticationToken(request.username, request.password)

        val authentication = authenticationManager.authenticate(authToken)
        SecurityContextHolder.getContext().authentication = authentication

        val session = servletRequest.getSession(true)
        session.setAttribute("SPRING_SECURITY_CONTEXT", SecurityContextHolder.getContext())

        val response = AuthDto.SimpleMessageResponse(
            message = "Login successful"
        )

        return ResponseEntity.ok(response)
    }

    /**
     * Terminates the current HTTP session and clears security context.
     */
    @PostMapping("/logout")
    fun logout(servletRequest: HttpServletRequest): ResponseEntity<AuthDto.SimpleMessageResponse> {
        val session = servletRequest.getSession(false)
        session?.invalidate()
        SecurityContextHolder.clearContext()

        val response = AuthDto.SimpleMessageResponse(
            message = "Logout successful"
        )

        return ResponseEntity.ok(response)
    }
}

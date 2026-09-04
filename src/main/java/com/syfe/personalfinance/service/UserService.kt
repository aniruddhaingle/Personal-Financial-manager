package com.syfe.personalfinance.service

import com.syfe.personalfinance.dto.AuthDto
import com.syfe.personalfinance.entity.User
import org.springframework.security.core.userdetails.UserDetailsService

/**
 * Service handling user registration, identity retrieval, and Spring Security authentication integration.
 */
interface UserService : UserDetailsService {
    /**
     * Registers a new user with BCrypt hashed credentials.
     *
     * @param request the registration details containing email, password, full name, and phone
     * @return the created user profile
     * @throws com.syfe.personalfinance.exception.ConflictException if the username/email is already taken
     */
    fun register(request: AuthDto.RegisterRequest): AuthDto.UserResponse

    /**
     * Resolves and returns the JPA entity of the currently authenticated user from SecurityContext.
     *
     * @return the User entity
     * @throws com.syfe.personalfinance.exception.UnauthorizedException if no valid user is authenticated
     */
    fun getAuthenticatedUserEntity(): User
}


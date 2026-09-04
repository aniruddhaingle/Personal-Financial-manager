package com.syfe.personalfinance.service.impl

import com.syfe.personalfinance.dto.AuthDto
import com.syfe.personalfinance.entity.User
import com.syfe.personalfinance.exception.ConflictException
import com.syfe.personalfinance.exception.ResourceNotFoundException
import com.syfe.personalfinance.mapper.UserMapper
import com.syfe.personalfinance.repository.UserRepository
import com.syfe.personalfinance.service.UserService
import com.syfe.personalfinance.util.SecurityUtils
import org.slf4j.LoggerFactory
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.Collections

@Service
class UserServiceImpl(
    private val userRepository: UserRepository,
    private val userMapper: UserMapper,
    private val passwordEncoder: PasswordEncoder
) : UserService {

    private val log = LoggerFactory.getLogger(UserServiceImpl::class.java)

    @Transactional
    override fun register(request: AuthDto.RegisterRequest): AuthDto.UserResponse {
        log.info("Processing registration request for username: {}", request.username)

        if (userRepository.existsByUsername(request.username)) {
            log.warn("Registration failed: Username {} already exists", request.username)
            throw ConflictException("Username/Email already exists")
        }

        val user = userMapper.toEntity(request) ?: throw IllegalStateException("Failed to map user entity")
        user.password = passwordEncoder.encode(request.password) // Enforce BCrypt password hashing

        val savedUser = userRepository.save(user)
        log.info("User registered successfully with ID: {}", savedUser.id)

        return userMapper.toResponse(savedUser) ?: throw IllegalStateException("Failed to map user response")
    }

    @Transactional(readOnly = true)
    override fun loadUserByUsername(username: String): UserDetails {
        log.debug("Loading user security context details for username: {}", username)

        val user = userRepository.findByUsername(username)
            .orElseThrow {
                log.warn("User auth retrieval failed for username: {}", username)
                UsernameNotFoundException("User not found with username: $username")
            }

        return org.springframework.security.core.userdetails.User(
            user.username,
            user.password,
            Collections.singletonList(SimpleGrantedAuthority("ROLE_USER"))
        )
    }

    @Transactional(readOnly = true)
    override fun getAuthenticatedUserEntity(): User {
        val username = SecurityUtils.getCurrentUsername()
        return userRepository.findByUsername(username)
            .orElseThrow {
                log.error("Authenticated user context username: {} not found in database", username)
                ResourceNotFoundException("Authenticated user not found")
            }
    }
}

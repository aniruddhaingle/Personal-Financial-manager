package com.syfe.personalfinance.service

import com.syfe.personalfinance.dto.AuthDto
import com.syfe.personalfinance.dto.AuthDto.UserResponse
import com.syfe.personalfinance.entity.User
import com.syfe.personalfinance.exception.ConflictException
import com.syfe.personalfinance.mapper.UserMapper
import com.syfe.personalfinance.repository.UserRepository
import com.syfe.personalfinance.service.impl.UserServiceImpl
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.crypto.password.PasswordEncoder
import java.util.Optional

@ExtendWith(MockitoExtension::class)
class UserServiceTest {

    @Mock
    private lateinit var userRepository: UserRepository

    @Mock
    private lateinit var userMapper: UserMapper

    @Mock
    private lateinit var passwordEncoder: PasswordEncoder

    private lateinit var userService: UserService

    @BeforeEach
    fun setUp() {
        userService = UserServiceImpl(userRepository, userMapper, passwordEncoder)
    }

    @Test
    fun register_Success() {
        val request = AuthDto.RegisterRequest(
            username = "test@syfe.com",
            password = "SecurePassword123!",
            fullName = "Test User"
        )

        val userEntity = User(
            username = "test@syfe.com",
            password = "encodedPassword",
            fullName = "Test User"
        )

        val expectedResponse = UserResponse(
            id = 1L,
            username = "test@syfe.com",
            fullName = "Test User"
        )

        whenever(userRepository.existsByUsername(request.username)).thenReturn(false)
        whenever(userMapper.toEntity(request)).thenReturn(userEntity)
        whenever(passwordEncoder.encode(request.password)).thenReturn("encodedPassword")
        whenever(userRepository.save(any<User>())).thenReturn(userEntity)
        whenever(userMapper.toResponse(any<User>())).thenReturn(expectedResponse)

        val actualResponse = userService.register(request)

        assertNotNull(actualResponse)
        assertEquals("test@syfe.com", actualResponse.username)
        verify(userRepository, times(1)).save(any<User>())
    }

    @Test
    fun register_ConflictingUsername_ThrowsException() {
        val request = AuthDto.RegisterRequest(
            username = "duplicate@syfe.com",
            password = "SecurePassword123!",
            fullName = "Duplicate User"
        )

        whenever(userRepository.existsByUsername(request.username)).thenReturn(true)

        assertThrows(ConflictException::class.java) { userService.register(request) }
        verify(userRepository, never()).save(any<User>())
    }

    @Test
    fun loadUserByUsername_Success() {
        val userEntity = User(
            username = "user@syfe.com",
            password = "encoded_pass"
        )

        whenever(userRepository.findByUsername("user@syfe.com")).thenReturn(Optional.of(userEntity))

        val userDetails = userService.loadUserByUsername("user@syfe.com")

        assertNotNull(userDetails)
        assertEquals("user@syfe.com", userDetails.username)
        assertEquals("encoded_pass", userDetails.password)
    }

    @Test
    fun loadUserByUsername_NotFound_ThrowsException() {
        whenever(userRepository.findByUsername("nonexistent@syfe.com")).thenReturn(Optional.empty())

        assertThrows(UsernameNotFoundException::class.java) { userService.loadUserByUsername("nonexistent@syfe.com") }
    }
}

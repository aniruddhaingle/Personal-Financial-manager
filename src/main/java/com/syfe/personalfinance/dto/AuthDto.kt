package com.syfe.personalfinance.dto

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank

class AuthDto {

    data class RegisterRequest(
        @field:NotBlank(message = "Username (email) is required")
        @field:Email(message = "Username must be a valid email address")
        val username: String = "",

        @field:NotBlank(message = "Password is required")
        val password: String = "",

        @field:NotBlank(message = "Full name is required")
        val fullName: String = "",

        val phoneNumber: String? = null
    )

    data class LoginRequest(
        @field:NotBlank(message = "Username (email) is required")
        @field:Email(message = "Username must be a valid email address")
        val username: String = "",

        @field:NotBlank(message = "Password is required")
        val password: String = ""
    )

    data class UserResponse(
        val id: Long? = null,
        val username: String = "",
        val fullName: String? = null,
        val phoneNumber: String? = null
    )

    data class RegisterResponse(
        val message: String = "",
        val userId: Long? = null
    )

    data class SimpleMessageResponse(
        val message: String = ""
    )
}

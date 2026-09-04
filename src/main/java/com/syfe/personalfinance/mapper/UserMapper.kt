package com.syfe.personalfinance.mapper

import com.syfe.personalfinance.dto.AuthDto
import com.syfe.personalfinance.entity.User
import org.springframework.stereotype.Component

@Component
class UserMapper {

    fun toResponse(user: User?): AuthDto.UserResponse? {
        if (user == null) {
            return null
        }
        return AuthDto.UserResponse(
            id = user.id,
            username = user.username,
            fullName = user.fullName,
            phoneNumber = user.phoneNumber
        )
    }

    fun toEntity(request: AuthDto.RegisterRequest?): User? {
        if (request == null) {
            return null
        }
        return User(
            username = request.username,
            fullName = request.fullName,
            phoneNumber = request.phoneNumber
        )
    }
}

package com.syfe.personalfinance.util

import com.syfe.personalfinance.exception.UnauthorizedException
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetails

object SecurityUtils {

    fun getCurrentUsername(): String {
        val authentication = SecurityContextHolder.getContext().authentication
        if (authentication == null || !authentication.isAuthenticated || "anonymousUser" == authentication.principal) {
            throw UnauthorizedException("Session is not authenticated: Access denied")
        }

        return when (val principal = authentication.principal) {
            is UserDetails -> principal.username
            is String -> principal
            else -> throw UnauthorizedException("Invalid authentication credentials")
        }
    }
}

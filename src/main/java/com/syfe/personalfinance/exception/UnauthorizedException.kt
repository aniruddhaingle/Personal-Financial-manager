package com.syfe.personalfinance.exception

import org.springframework.http.HttpStatus

class UnauthorizedException(message: String) : CustomException(message, HttpStatus.UNAUTHORIZED)

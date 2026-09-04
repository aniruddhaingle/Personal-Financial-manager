package com.syfe.personalfinance.exception

import org.springframework.http.HttpStatus

class ForbiddenException(message: String) : CustomException(message, HttpStatus.FORBIDDEN)

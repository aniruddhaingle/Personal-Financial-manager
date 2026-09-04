package com.syfe.personalfinance.exception

import org.springframework.http.HttpStatus

class ConflictException(message: String) : CustomException(message, HttpStatus.CONFLICT)

package com.syfe.personalfinance.exception

import org.springframework.http.HttpStatus

class BadRequestException(message: String) : CustomException(message, HttpStatus.BAD_REQUEST)

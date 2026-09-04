package com.syfe.personalfinance.exception

import org.springframework.http.HttpStatus

class ResourceNotFoundException(message: String) : CustomException(message, HttpStatus.NOT_FOUND)

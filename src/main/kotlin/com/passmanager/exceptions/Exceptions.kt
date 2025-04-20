package com.passmanager.exceptions

class UnauthorizedException : RuntimeException("Unauthorized")
class NotFoundException(message: String) : RuntimeException(message) 
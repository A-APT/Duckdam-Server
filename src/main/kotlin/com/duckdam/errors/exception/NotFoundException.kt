package com.duckdam.errors.exception

import java.lang.RuntimeException

class NotFoundException(message: String) : RuntimeException(message)
package com.duckdam.errors.exception

import java.lang.RuntimeException

class UnknownException(message: String) : RuntimeException(message)
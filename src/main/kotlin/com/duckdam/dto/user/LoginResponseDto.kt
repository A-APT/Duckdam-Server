package com.duckdam.dto.user

data class LoginResponseDto (
    val token: String,
    val refreshToken: String
)

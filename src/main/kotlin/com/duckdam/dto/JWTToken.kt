package com.duckdam.dto

data class JWTToken (
    val token: String,
    val refreshToken: String
)

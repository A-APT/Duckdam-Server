package com.duckdam

import com.duckdam.dto.user.RegisterDto

object MockDto {
    val mockRegisterDto = RegisterDto(
        name = "je",
        password = "test",
        email = "email@konkuk.ac.kr",
        profile = null,
    )
}
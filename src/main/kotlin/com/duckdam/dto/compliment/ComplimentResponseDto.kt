package com.duckdam.dto.compliment

import java.util.*

data class ComplimentResponseDto (
    val fromId: Long,
    val toId: Long,
    val stickerNum: Int,
    val message: String,
    val date: Date,
)

package com.duckdam.dto.compliment

import java.util.*

data class ComplimentResponseDto (
    val fromId: Long,
    val fromName: String,
    val toId: Long,
    var toName: String,
    val stickerNum: Int,
    val message: String,
    val date: Date,
)

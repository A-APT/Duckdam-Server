package com.duckdam.domain.compliment

import com.duckdam.dto.compliment.ComplimentResponseDto
import java.util.*
import javax.persistence.*

@Entity
class Compliment (
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = -1,

    @Column(nullable = false)
    var stickerNum: Int,

    @Column(nullable = false)
    var fromId: Long, // uid

    @Column(nullable = false)
    var toId: Long, // uid

    @Column(nullable = false)
    var message: String,

    @Column(nullable = false)
    var date: Date

) {

    fun toComplimentResponseDto(): ComplimentResponseDto {
        return ComplimentResponseDto(
            fromId, toId, stickerNum, message, date
        )
    }

}

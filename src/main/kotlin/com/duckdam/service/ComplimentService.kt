package com.duckdam.service

import com.duckdam.domain.compliment.Compliment
import com.duckdam.domain.compliment.ComplimentRepository
import com.duckdam.domain.user.UserRepository
import com.duckdam.dto.compliment.ComplimentRequestDto
import com.duckdam.errors.exception.NotFoundException
import org.springframework.stereotype.Service
import java.util.*

@Service
class ComplimentService (
    private val complimentRepository: ComplimentRepository,
    private val userRepository: UserRepository,
) {
    fun generateCompliment(fromId: Long, complimentRequestDto: ComplimentRequestDto): Long {
        runCatching {
            userRepository.findById(complimentRequestDto.toId).get()
        }.onFailure {
            throw NotFoundException("User [${complimentRequestDto.toId}] was not registered.")
        }
        return return complimentRepository.save(Compliment(
            stickerNum = complimentRequestDto.stickerNum,
            fromId = fromId,
            toId = complimentRequestDto.toId,
            message = complimentRequestDto.message,
            date = Date()
        )).id
    }
}

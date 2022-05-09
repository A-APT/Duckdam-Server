package com.duckdam.service

import com.duckdam.domain.compliment.Compliment
import com.duckdam.domain.compliment.ComplimentRepository
import com.duckdam.domain.user.UserRepository
import com.duckdam.dto.compliment.ComplimentRequestDto
import com.duckdam.dto.compliment.ComplimentResponseDto
import com.duckdam.errors.exception.NotFoundException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
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

    fun findCompliments(toId: Long): ResponseEntity<List<ComplimentResponseDto>> {
        val complimentList: List<ComplimentResponseDto> = complimentRepository
            .findAllByToId(toId = toId)
            .map { it.toComplimentResponseDto() }
            .toList()
        return ResponseEntity
            .status(HttpStatus.OK)
            .body(
                complimentList
            )
    }

    fun findComplimentsByFromAndTo(fromId: Long, toId: Long): ResponseEntity<List<ComplimentResponseDto>> {
        runCatching {
            userRepository.findById(toId).get()
        }.onFailure {
            throw NotFoundException("User [${toId}] was not registered.")
        }
        val complimentList: List<ComplimentResponseDto> = complimentRepository
            .findAllByFromIdAndToId(fromId = fromId, toId = toId)
            .map { it.toComplimentResponseDto() }
            .toList()
        return ResponseEntity
            .status(HttpStatus.OK)
            .body(
                complimentList
            )
    }
}

package com.duckdam.controller

import com.duckdam.dto.compliment.ComplimentRequestDto
import com.duckdam.dto.compliment.ComplimentResponseDto
import com.duckdam.security.JWTTokenProvider
import com.duckdam.service.ComplimentService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
class ComplimentController (
    private val complimentService: ComplimentService,
    private val jwtTokenProvider: JWTTokenProvider,
){

    @PostMapping("/compliment")
    fun generateCompliment(@RequestHeader httpHeaders: Map<String, String>, @RequestBody complimentRequestDto: ComplimentRequestDto): ResponseEntity<Unit> {
        val uid: Long = jwtTokenProvider.getUserPK(jwtTokenProvider.getTokenFromHeader(httpHeaders)!!).toLong()
        complimentService.generateCompliment(uid, complimentRequestDto)
        return ResponseEntity.noContent().build()
    }

    @GetMapping("/compliments")
    fun findCompliments(@RequestHeader httpHeaders: Map<String, String>): ResponseEntity<List<ComplimentResponseDto>> {
        val uid: Long = jwtTokenProvider.getUserPK(jwtTokenProvider.getTokenFromHeader(httpHeaders)!!).toLong()
        return complimentService.findCompliments(uid)
    }

    @GetMapping("/compliments/{toId}")
    fun findComplimentsByFromAndTo(@RequestHeader httpHeaders: Map<String, String>, @PathVariable toId: Long): ResponseEntity<List<ComplimentResponseDto>> {
        val uid: Long = jwtTokenProvider.getUserPK(jwtTokenProvider.getTokenFromHeader(httpHeaders)!!).toLong()
        return complimentService.findComplimentsByFromAndTo(fromId = uid, toId = toId)
    }
}
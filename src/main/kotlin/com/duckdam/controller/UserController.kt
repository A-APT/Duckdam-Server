package com.duckdam.controller

import com.duckdam.dto.JWTToken
import com.duckdam.dto.user.*
import com.duckdam.security.JWTTokenProvider
import com.duckdam.service.UserService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
class UserController(
    private val userService: UserService,
    private val jwtTokenProvider: JWTTokenProvider,
) {

    @PostMapping("/user/register")
    fun register(@RequestBody registerDto: RegisterDto): ResponseEntity<Unit> {
        userService.register(registerDto)
        return ResponseEntity.noContent().build()
    }

    @PostMapping("/user/login")
    fun login(@RequestBody loginRequestDto: LoginRequestDto): ResponseEntity<LoginResponseDto> {
        return userService.login(loginRequestDto)
    }

    @PostMapping("/user/refresh")
    fun refreshToken(@RequestBody refreshToken: String): ResponseEntity<JWTToken> {
        return userService.refreshToken(refreshToken)
    }

    @GetMapping("/users/{query}")
    fun searchByName(@RequestHeader httpHeaders: Map<String, String>, @PathVariable query: String): ResponseEntity<List<UserResponseDto>> {
        return userService.searchByName(query)
    }

    @GetMapping("/user/stickers")
    fun getStickerList(@RequestHeader httpHeaders: Map<String, String>): ResponseEntity<List<Boolean>> {
        val uid: Long = jwtTokenProvider.getUserPK(jwtTokenProvider.getTokenFromHeader(httpHeaders)!!).toLong()
        return userService.getStickerList(uid)
    }
}

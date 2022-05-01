package com.duckdam.controller

import com.duckdam.dto.JWTToken
import com.duckdam.dto.user.*
import com.duckdam.service.UserService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class UserController(private val userService: UserService) {

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
}

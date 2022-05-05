package com.duckdam.service

import com.duckdam.domain.user.User
import com.duckdam.domain.user.UserRepository
import com.duckdam.dto.JWTToken
import com.duckdam.dto.user.LoginRequestDto
import com.duckdam.dto.user.LoginResponseDto
import com.duckdam.dto.user.RegisterDto
import com.duckdam.errors.exception.ConflictException
import com.duckdam.errors.exception.NotFoundException
import com.duckdam.security.JWTTokenProvider
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service

@Service
class UserService (
    private val userRepository: UserRepository,
    private val jwtTokenProvider: JWTTokenProvider,
    ) {

    fun register(registerDto: RegisterDto): Long {
        // check duplicate
        runCatching {
            userRepository.findByEmail(registerDto.email)
        }.onSuccess {
            throw ConflictException("User email [${registerDto.email}] is already registered.")
        }

        // save to server
        return userRepository.save(
            User(
                name = registerDto.name,
                password = registerDto.password,
                email = registerDto.email,
                profile = registerDto.profile,
                sticker = "00000",
                roles = setOf("ROLE_USER")
            )
        ).id
    }

    fun login(loginRequestDto: LoginRequestDto): ResponseEntity<LoginResponseDto> {
        // Find user
        lateinit var user: User
        runCatching {
            userRepository.findByEmail(loginRequestDto.email)
        }.onSuccess {
            user = it
        }.onFailure {
            throw NotFoundException("User [${loginRequestDto.email}] was not registered.")
        }

        // Check password
        if (user.password != loginRequestDto.password) {
            throw NotFoundException("User email or password was wrong.")
        }

        // Generate JWT token
        val jwtToken: JWTToken = jwtTokenProvider.generateToken(user.email, user.roles.toList())
        return ResponseEntity
            .status(HttpStatus.OK)
            .body(
                LoginResponseDto(token = jwtToken.token, refreshToken = jwtToken.refreshToken)
            )
    }

    fun refreshToken(refreshToken: String): ResponseEntity<JWTToken> { // refresh JWT token
        val jwtToken: JWTToken = jwtTokenProvider.refreshToken(refreshToken)
        return ResponseEntity
            .status(HttpStatus.OK)
            .body(
                JWTToken(token = jwtToken.token, refreshToken = jwtToken.refreshToken)
            )
    }
}

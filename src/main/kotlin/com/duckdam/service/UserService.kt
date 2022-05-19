package com.duckdam.service

import com.duckdam.domain.user.User
import com.duckdam.domain.user.UserRepository
import com.duckdam.dto.JWTToken
import com.duckdam.dto.user.LoginRequestDto
import com.duckdam.dto.user.LoginResponseDto
import com.duckdam.dto.user.RegisterDto
import com.duckdam.dto.user.UserResponseDto
import com.duckdam.errors.exception.ConflictException
import com.duckdam.errors.exception.NotFoundException
import com.duckdam.security.JWTTokenProvider
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class UserService (
    private val userRepository: UserRepository,
    private val jwtTokenProvider: JWTTokenProvider,
    ) {

    fun register(registerDto: RegisterDto): Long {
        // check duplicate for email and name
        runCatching {
            userRepository.findByEmail(registerDto.email)
        }.onSuccess {
            throw ConflictException("User email [${registerDto.email}] is already registered.")
        }
        runCatching {
            userRepository.findByName(registerDto.name)
        }.onSuccess {
            throw ConflictException("User name [${registerDto.name}] is already registered.")
        }
        val before = LocalDate.now().minusDays(1) // yesterday
        // save to server
        return userRepository.save(
            User(
                name = registerDto.name,
                password = registerDto.password,
                email = registerDto.email,
                profile = registerDto.profile,
                sticker = "00000",
                latestSlot = before,
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
        val jwtToken: JWTToken = jwtTokenProvider.generateToken(user.id.toString(), user.roles.toList())
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

    fun searchByName(query: String): ResponseEntity<List<UserResponseDto>> {
        val searchResult: MutableList<User> = userRepository.findByNameContains(query)
        val responseList: MutableList<UserResponseDto> = mutableListOf()
        searchResult.forEach {
            val user: User = userRepository.findById(it.id).get()
            responseList.add(
                UserResponseDto(
                    uid =  user.id,
                    name = user.name,
                    profile = user.profile
                ))
        }
        return ResponseEntity
            .status(HttpStatus.OK)
            .body(
                responseList
            )
    }

    fun getStickerList(userId: Long): ResponseEntity<List<Boolean>> {
        val sticker: String = userRepository.findById(userId).get().sticker
        val stickerList: MutableList<Boolean> = mutableListOf(true, true, true, true, true) // set default 5 stickers
        sticker.forEach { stickerList.add(it == '1') } // 1 is ture(have)
        return ResponseEntity
            .status(HttpStatus.OK)
            .body(
                stickerList
            )
    }

    fun isEligibleForSlot(userId: Long): ResponseEntity<Boolean> { /* as of today */
        val latestSlot: LocalDate = userRepository.findById(userId).get().latestSlot
        val today: LocalDate = LocalDate.now()
        return ResponseEntity
            .status(HttpStatus.OK)
            .body(
                today.isAfter(latestSlot)
            )
    }
}

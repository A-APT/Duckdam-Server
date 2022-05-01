package com.duckdam.controller

import com.duckdam.MockDto
import com.duckdam.domain.user.UserRepository
import com.duckdam.dto.JWTToken
import com.duckdam.dto.user.*
import com.duckdam.errors.exception.NotFoundException
import com.duckdam.errors.exception.UnauthorizedException
import com.duckdam.service.UserService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.http.HttpStatus
import org.springframework.test.context.junit.jupiter.SpringExtension

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ExtendWith(SpringExtension::class)
class UserControllerTest {
    @LocalServerPort
    private var port: Int = -1

    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var userService: UserService

    @Autowired
    private lateinit var restTemplate: TestRestTemplate

    private lateinit var baseAddress: String

    private val mockRegisterDto: RegisterDto = MockDto.mockRegisterDto

    @BeforeEach
    @AfterEach
    fun initTest() {
        baseAddress = "http://localhost:${port}"
        userRepository.deleteAll()
    }

    fun registerAndLogin(): String {
        userService.register(mockRegisterDto)
        return userService
            .login(LoginRequestDto(mockRegisterDto.email, mockRegisterDto.password))
            .body!!.token
    }

    @Test
    fun is_register_works_well() {
        // arrange
        val registerDto = mockRegisterDto

        // act, assert
        restTemplate
            .postForEntity("${baseAddress}/user/register", registerDto, Unit::class.java)
            .apply {
                assertThat(statusCode).isEqualTo(HttpStatus.NO_CONTENT)
            }
    }

    @Test
    fun is_register_works_on_duplicate() {
        // arrange
        userService.register(mockRegisterDto)

        // act, assert
        restTemplate
            .postForEntity("${baseAddress}/user/register", mockRegisterDto, Unit::class.java)
            .apply {
                assertThat(statusCode).isEqualTo(HttpStatus.CONFLICT)
            }
    }

    @Test
    fun is_login_works_well() {
        // arrange
        userService.register(mockRegisterDto)
        val loginRequestDto = LoginRequestDto(
            email = mockRegisterDto.email,
            password = mockRegisterDto.password
        )

        // act, assert
        restTemplate
            .postForEntity("${baseAddress}/user/login", loginRequestDto, LoginResponseDto::class.java)
            .apply {
                assertThat(statusCode).isEqualTo(HttpStatus.OK)
            }
    }

    @Test
    fun is_login_works_on_invalidPW() {
        // arrange
        userService.register(mockRegisterDto)
        val loginRequestDto = LoginRequestDto(
            email = mockRegisterDto.email,
            password = "invalid" // invalid password
        )

        // act, assert
        restTemplate
            .postForEntity("${baseAddress}/user/login", loginRequestDto, NotFoundException::class.java)
            .apply {
                assertThat(statusCode).isEqualTo(HttpStatus.NOT_FOUND)
            }
    }

    @Test
    fun is_login_works_on_NOTFOUND_user() {
        // not register any user
        // arrange
        val loginRequestDto = LoginRequestDto(
            email = mockRegisterDto.email,
            password = "invalid"
        )

        // act, assert
        restTemplate
            .postForEntity("${baseAddress}/user/login", loginRequestDto, NotFoundException::class.java)
            .apply {
                assertThat(statusCode).isEqualTo(HttpStatus.NOT_FOUND)
            }
    }

    @Test
    fun is_refreshToken_works_well() {
        // arrange
        userService.register(mockRegisterDto)
        val loginRequestDto = LoginRequestDto(
            email = mockRegisterDto.email,
            password = mockRegisterDto.password
        )
        val loginResponseDto = userService.login(loginRequestDto)

        // act, assert
        restTemplate
            .postForEntity("${baseAddress}/user/refresh", loginResponseDto.body!!.refreshToken, JWTToken::class.java)
            .apply {
                assertThat(statusCode).isEqualTo(HttpStatus.OK)
            }
    }

    @Test
    fun is_refreshToken_works_on_invalidToken() {
        // arrange
        registerAndLogin()

        // act, assert
        restTemplate
            .postForEntity("${baseAddress}/user/refresh", "invalid-token", UnauthorizedException::class.java)
            .body!!
            .apply {
                assertThat(message).isEqualTo("Failed when refresh token.")
            }
    }
}

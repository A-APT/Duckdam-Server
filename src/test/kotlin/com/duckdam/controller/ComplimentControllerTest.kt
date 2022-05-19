package com.duckdam.controller

import com.duckdam.MockDto
import com.duckdam.domain.compliment.ComplimentRepository
import com.duckdam.domain.user.UserRepository
import com.duckdam.dto.compliment.ComplimentRequestDto
import com.duckdam.dto.compliment.ComplimentResponseDto
import com.duckdam.dto.user.LoginRequestDto
import com.duckdam.dto.user.RegisterDto
import com.duckdam.errors.exception.ForbiddenException
import com.duckdam.service.ComplimentService
import com.duckdam.service.UserService
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.fail
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.test.context.junit.jupiter.SpringExtension

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ExtendWith(SpringExtension::class)
class ComplimentControllerTest {
    @LocalServerPort
    private var port: Int = -1

    @Autowired
    private lateinit var complimentService: ComplimentService

    @Autowired
    private lateinit var userService: UserService

    @Autowired
    private lateinit var complimentRepository: ComplimentRepository

    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var restTemplate: TestRestTemplate

    private lateinit var baseAddress: String

    private val mockRegisterDto: RegisterDto = MockDto.mockRegisterDto
    private var mockUid: Long = 0 // testing

    @BeforeEach
    @AfterEach
    fun initTest() {
        baseAddress = "http://localhost:${port}"
        userRepository.deleteAll()
        complimentRepository.deleteAll()
    }

    fun registerAndLogin(): String {
        mockUid = userService.register(mockRegisterDto)
        return userService
            .login(LoginRequestDto(mockRegisterDto.email, mockRegisterDto.password))
            .body!!.token
    }

    @Test
    fun is_generateCompliment_throws_on_no_auth_token() {
        // arrange
        val httpHeaders = HttpHeaders()
        val httpEntity = HttpEntity(null, httpHeaders)

        // act, assert
        restTemplate
            .exchange("${baseAddress}/compliment", HttpMethod.POST, httpEntity, Unit::class.java)
            .apply {
                assertThat(statusCode).isEqualTo(HttpStatus.FORBIDDEN)
            }
    }

    @Test
    fun is_generateCompliment_works_well() {
        // arrange
        val token: String = registerAndLogin()
        val uid2: Long = userService.register(mockRegisterDto.copy(email = "email", name = "new"))
        val httpHeaders = HttpHeaders().apply {
            this["Authorization"] = "Bearer $token"
        }
        val complimentRequestDto = ComplimentRequestDto(toId = uid2, stickerNum = 0, message = "thanks")
        val httpEntity = HttpEntity(complimentRequestDto, httpHeaders)

        // act, assert
        restTemplate
            .exchange("${baseAddress}/compliment", HttpMethod.POST, httpEntity, Unit::class.java)
            .apply {
                assertThat(statusCode).isEqualTo(HttpStatus.NO_CONTENT)
            }
    }

    @Test
    fun is_generateCompliment_throws_on_invalid_toId() {
        // arrange
        val token: String = registerAndLogin()
        val invalidId: Long = -10
        val httpHeaders = HttpHeaders().apply {
            this["Authorization"] = "Bearer $token"
        }
        val complimentRequestDto = ComplimentRequestDto(toId = invalidId, stickerNum = 0, message = "thanks")
        val httpEntity = HttpEntity(complimentRequestDto, httpHeaders)

        // act, assert
        restTemplate
            .exchange("${baseAddress}/compliment", HttpMethod.POST, httpEntity, Unit::class.java)
            .apply {
                assertThat(statusCode).isEqualTo(HttpStatus.NOT_FOUND)
            }
    }

    @Test
    fun is_findCompliments_throws_on_no_auth_token() {
        // arrange
        val httpHeaders = HttpHeaders()
        val httpEntity = HttpEntity(null, httpHeaders)

        // act, assert
        restTemplate
            .exchange("${baseAddress}/compliments", HttpMethod.GET, httpEntity, Unit::class.java)
            .apply {
                assertThat(statusCode).isEqualTo(HttpStatus.FORBIDDEN)
            }
    }
    
    @Test
    fun is_findCompliments_works_well() {
        // arrange
        val token: String = registerAndLogin()
        val uid2: Long = userService.register(mockRegisterDto.copy(email = "email", name = "new"))
        val httpHeaders = HttpHeaders().apply {
            this["Authorization"] = "Bearer $token"
        }
        val httpEntity = HttpEntity(null, httpHeaders)
        val complimentRequestDto = ComplimentRequestDto(toId = mockUid, stickerNum = 0, message = "thanks")
        complimentService.generateCompliment(fromId = uid2, complimentRequestDto)

        // act, assert
        restTemplate
            .exchange("${baseAddress}/compliments", HttpMethod.GET, httpEntity, Array<ComplimentResponseDto>::class.java)
            .apply {
                assertThat(statusCode).isEqualTo(HttpStatus.OK)
                assertThat(body!!.size).isEqualTo(1)
            }
    }

    @Test
    fun is_findComplimentsByFromAndTo_throws_on_no_auth_token() {
        // arrange
        val httpHeaders = HttpHeaders()
        val httpEntity = HttpEntity(null, httpHeaders)
        val uid2: Long = 10

        // act, assert
        restTemplate
            .exchange("${baseAddress}/compliments/$uid2", HttpMethod.GET, httpEntity, Unit::class.java)
            .apply {
                assertThat(statusCode).isEqualTo(HttpStatus.FORBIDDEN)
            }
    }

    @Test
    fun is_findComplimentsByFromAndTo_works_well() {
        // arrange
        val token: String = registerAndLogin()
        val uid2: Long = userService.register(mockRegisterDto.copy(email = "email", name = "new"))
        val httpHeaders = HttpHeaders().apply {
            this["Authorization"] = "Bearer $token"
        }
        val httpEntity = HttpEntity(null, httpHeaders)
        val complimentRequestDto = ComplimentRequestDto(toId = uid2, stickerNum = 0, message = "thanks")
        complimentService.generateCompliment(fromId = mockUid, complimentRequestDto)

        // act, assert
        restTemplate
            .exchange("${baseAddress}/compliments/$uid2", HttpMethod.GET, httpEntity, Array<ComplimentResponseDto>::class.java)
            .apply {
                assertThat(statusCode).isEqualTo(HttpStatus.OK)
                assertThat(body!!.size).isEqualTo(1)
            }
    }

    @Test
    fun is_findComplimentsByFromAndTo_throws_on_invalid_toId() {
        // arrange
        val token: String = registerAndLogin()
        val uid2: Long = userService.register(mockRegisterDto.copy(email = "email", name = "new"))
        val httpHeaders = HttpHeaders().apply {
            this["Authorization"] = "Bearer $token"
        }
        val httpEntity = HttpEntity(null, httpHeaders)
        val complimentRequestDto = ComplimentRequestDto(toId = uid2, stickerNum = 0, message = "thanks")
        complimentService.generateCompliment(fromId = mockUid, complimentRequestDto)
        val invalidId: Long = 10

        // act, assert
        restTemplate
            .exchange("${baseAddress}/compliments/$invalidId", HttpMethod.GET, httpEntity, Unit::class.java)
            .apply {
                assertThat(statusCode).isEqualTo(HttpStatus.NOT_FOUND)
            }
    }

    @Test
    fun is_slot_throws_on_no_auth_token() {
        // arrange
        val httpHeaders = HttpHeaders()
        val httpEntity = HttpEntity(null, httpHeaders)

        // act, assert
        restTemplate
            .exchange("${baseAddress}/compliment/slot", HttpMethod.POST, httpEntity, Unit::class.java)
            .apply {
                assertThat(statusCode).isEqualTo(HttpStatus.FORBIDDEN)
            }
    }

    @Test
    fun is_slot_works_well() {
        // arrange
        val token: String = registerAndLogin()
        val httpHeaders = HttpHeaders().apply {
            this["Authorization"] = "Bearer $token"
        }
        val httpEntity = HttpEntity(null, httpHeaders)

        // act, assert
        restTemplate
            .exchange("${baseAddress}/compliment/slot", HttpMethod.POST, httpEntity, ComplimentResponseDto::class.java)
            .apply {
                assertThat(statusCode).isEqualTo(HttpStatus.OK)
            }
    }

    @Test
    fun is_slot_throws_when_ineligible_to_draw_today() {
        // arrange
        val token: String = registerAndLogin()
        val httpHeaders = HttpHeaders().apply {
            this["Authorization"] = "Bearer $token"
        }
        val httpEntity = HttpEntity(null, httpHeaders)

        complimentService.slot(mockUid)

        // act, assert
        restTemplate
            .exchange("${baseAddress}/compliment/slot", HttpMethod.POST, httpEntity, Unit::class.java)
            .apply {
                assertThat(statusCode).isEqualTo(HttpStatus.FORBIDDEN)
            }
    }
}
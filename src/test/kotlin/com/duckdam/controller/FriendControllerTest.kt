package com.duckdam.controller

import com.duckdam.MockDto
import com.duckdam.domain.friend.FriendRepository
import com.duckdam.domain.user.UserRepository
import com.duckdam.dto.user.UserResponseDto
import com.duckdam.dto.user.LoginRequestDto
import com.duckdam.dto.user.RegisterDto
import com.duckdam.service.FriendService
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
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.test.context.junit.jupiter.SpringExtension

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ExtendWith(SpringExtension::class)
class FriendControllerTest {
    @LocalServerPort
    private var port: Int = -1

    @Autowired
    private lateinit var friendService: FriendService

    @Autowired
    private lateinit var userService: UserService

    @Autowired
    private lateinit var friendRepository: FriendRepository

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
        friendRepository.deleteAll()
    }

    fun registerAndLogin(): String {
        mockUid = userService.register(mockRegisterDto)
        return userService
            .login(LoginRequestDto(mockRegisterDto.email, mockRegisterDto.password))
            .body!!.token
    }

    @Test
    fun is_followFriend_throw_on_no_auth_token() {
        // arrange
        val token: String = registerAndLogin()
        val httpHeaders = HttpHeaders()
        val httpEntity = HttpEntity(null, httpHeaders)
        val uid2: Long = userService.register(mockRegisterDto.copy(email = "email"))

        // act, assert
        restTemplate
            .exchange("${baseAddress}/friend/follow/$uid2", HttpMethod.POST, httpEntity, Unit::class.java)
            .apply {
                assertThat(statusCode).isEqualTo(HttpStatus.FORBIDDEN)
            }
    }

    @Test
    fun is_followFriend_works_well() {
        // arrange
        val token: String = registerAndLogin()
        val httpHeaders = HttpHeaders().apply {
            this["Authorization"] = "Bearer $token"
        }
        val httpEntity = HttpEntity(null, httpHeaders)
        val uid2: Long = userService.register(mockRegisterDto.copy(email = "email"))

        // act, assert
        restTemplate
            .exchange("${baseAddress}/friend/follow/$uid2", HttpMethod.POST, httpEntity, Unit::class.java)
            .apply {
                assertThat(statusCode).isEqualTo(HttpStatus.NO_CONTENT)
                friendRepository.findByUidAndFriendId(mockUid, uid2)
            }
    }

    @Test
    fun is_followFriend_works_on_invalid_targetId() {
        // arrange
        val token: String = registerAndLogin()
        val httpHeaders = HttpHeaders().apply {
            this["Authorization"] = "Bearer $token"
        }
        val httpEntity = HttpEntity(null, httpHeaders)
        val invalidUid: Long = 10

        // act, assert
        restTemplate
            .exchange("${baseAddress}/friend/follow/$invalidUid", HttpMethod.POST, httpEntity, Unit::class.java)
            .apply {
                assertThat(statusCode).isEqualTo(HttpStatus.NOT_FOUND)
            }
    }

    @Test
    fun is_findMyFriends_throw_on_no_auth_token() {
        // arrange
        val token: String = registerAndLogin()
        val httpHeaders = HttpHeaders()
        val httpEntity = HttpEntity(null, httpHeaders)
        val uid2: Long = userService.register(mockRegisterDto.copy(email = "email"))
        friendService.followFriend(mockUid, uid2)

        // act, assert
        restTemplate
            .exchange("${baseAddress}/friend", HttpMethod.GET, httpEntity, Unit::class.java)
            .apply {
                assertThat(statusCode).isEqualTo(HttpStatus.FORBIDDEN)
            }
    }

    @Test
    fun is_findMyFriends_works_well() {
        // arrange
        val token: String = registerAndLogin()
        val httpHeaders = HttpHeaders().apply {
            this["Authorization"] = "Bearer $token"
        }
        val httpEntity = HttpEntity(null, httpHeaders)
        val uid2: Long = userService.register(mockRegisterDto.copy(email = "email"))
        friendService.followFriend(mockUid, uid2)

        // act, assert
        restTemplate
            .exchange("${baseAddress}/friend", HttpMethod.GET, httpEntity, Array<UserResponseDto>::class.java)
            .apply {
                assertThat(statusCode).isEqualTo(HttpStatus.OK)
                assertThat(body!!.size).isEqualTo(1)
            }
    }

}

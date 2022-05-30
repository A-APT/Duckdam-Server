package com.duckdam.service

import com.duckdam.MockDto
import com.duckdam.domain.user.User
import com.duckdam.domain.user.UserRepository
import com.duckdam.dto.JWTToken
import com.duckdam.dto.user.LoginRequestDto
import com.duckdam.dto.user.LoginResponseDto
import com.duckdam.dto.user.RegisterDto
import com.duckdam.dto.user.UserResponseDto
import com.duckdam.errors.exception.ConflictException
import com.duckdam.errors.exception.NotFoundException
import com.duckdam.errors.exception.UnauthorizedException
import com.duckdam.security.JWTTokenProvider
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.fail
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.time.LocalDate

@SpringBootTest
@ExtendWith(SpringExtension::class)
class UserServiceTest {

    @Autowired
    private lateinit var userService: UserService

    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var jwtTokenProvider: JWTTokenProvider

    private val mockRegisterDto: RegisterDto = MockDto.mockRegisterDto

    @BeforeEach
    @AfterEach
    fun init() {
        userRepository.deleteAll()
    }

    @Test
    fun is_register_works_well() {
        // act
        userService.register(mockRegisterDto)

        // assert
        val user: User = userRepository.findByEmail(mockRegisterDto.email)
        assertThat(user.email).isEqualTo(mockRegisterDto.email)
        assertThat(user.password).isEqualTo(mockRegisterDto.password)
    }

    @Test
    fun is_register_works_on_duplicate_email() {
        // act
        userService.register(mockRegisterDto)
        runCatching {
            userService.register(mockRegisterDto.copy(name = "new"))
        }.onSuccess {
            fail("This should be failed.")
        }.onFailure {
            assertThat(it is ConflictException).isEqualTo(true)
            assertThat(it.message).isEqualTo("User email [${mockRegisterDto.email}] is already registered.")
        }
    }

    @Test
    fun is_register_works_on_duplicate_name() {
        // act
        userService.register(mockRegisterDto)
        runCatching {
            userService.register(mockRegisterDto.copy(email = "new"))
        }.onSuccess {
            fail("This should be failed.")
        }.onFailure {
            assertThat(it is ConflictException).isEqualTo(true)
            assertThat(it.message).isEqualTo("User name [${mockRegisterDto.name}] is already registered.")
        }
    }

    @Test
    fun is_login_works_well() {
        // arrange
        val uid: Long = userService.register(mockRegisterDto)
        val loginRequestDto = LoginRequestDto(
            email = mockRegisterDto.email,
            password = mockRegisterDto.password
        )

        // act
        val loginResponseDto: LoginResponseDto? = userService.login(loginRequestDto).body

        // assert
        val user: User = userRepository.findByEmail(loginRequestDto.email)
        assertThat(user.email).isEqualTo(loginRequestDto.email)
        assertThat(user.password).isEqualTo(loginRequestDto.password)
        assertThat(loginResponseDto).isNotEqualTo(null)
        loginResponseDto?.apply {
            assertThat(jwtTokenProvider.verifyToken(token)).isEqualTo(true)
            assertThat(jwtTokenProvider.getUserPK(token).toLong()).isEqualTo(uid)
            assertThat(this.uid).isEqualTo(user.id)
            assertThat(name).isEqualTo(user.name)
            assertThat(profile).isEqualTo(user.profile)
        }
    }

    @Test
    fun is_login_works_well_when_invalidPW() {
        // arrange
        userService.register(mockRegisterDto)
        val loginRequestDto = LoginRequestDto(
            email = mockRegisterDto.email,
            password = "invalid"
        )

        // act, assert
        runCatching {
            userService.login(loginRequestDto).body
        }.onSuccess {
            fail("This should be failed.")
        }.onFailure {
            assertThat(it is NotFoundException).isEqualTo(true)
            assertThat(it.message).isEqualTo("User email or password was wrong.")
        }
    }

    @Test
    fun is_login_works_well_when_NotFoundUser() {
        // arrange
        val loginRequestDto = LoginRequestDto(
            email = mockRegisterDto.email,
            password = "invalid"
        )

        // act, assert
        runCatching {
            userService.login(loginRequestDto).body
        }.onSuccess {
            fail("This should be failed.")
        }.onFailure {
            assertThat(it is NotFoundException).isEqualTo(true)
            assertThat(it.message).isEqualTo("User [${loginRequestDto.email}] was not registered.")
        }
    }

    @Test
    fun is_refreshToken_works_well() {
        // arrange
        val uid: Long = userService.register(mockRegisterDto)
        val loginRequestDto = LoginRequestDto(
            email = mockRegisterDto.email,
            password = mockRegisterDto.password
        )
        val loginResponseDto: LoginResponseDto = userService.login(loginRequestDto).body!!

        // act
        val jwtToken: JWTToken = userService.refreshToken(loginResponseDto.refreshToken).body!!

        // assert
        jwtToken.apply {
            assertThat(jwtTokenProvider.verifyToken(token)).isEqualTo(true)
            assertThat(jwtTokenProvider.getUserPK(token).toLong()).isEqualTo(uid)
        }
    }

    @Test
    fun is_refreshToken_works_on_invalidToken() {
        // arrange
        userService.register(mockRegisterDto)
        val loginRequestDto = LoginRequestDto(
            email = mockRegisterDto.email,
            password = mockRegisterDto.password
        )
        userService.login(loginRequestDto).body!!

        // act
        runCatching {
            userService.refreshToken("invalid-token").body!!
        }.onSuccess {
            fail("This should be failed.")
        }.onFailure {
            assertThat(it is UnauthorizedException).isEqualTo(true)
            assertThat(it.message).isEqualTo("Failed when refresh token.")
        }
    }

    @Test
    fun is_searchByName_works_well() {
        // arrange
        userService.register(mockRegisterDto.copy(email = "email1", name = "test1"))
        userService.register(mockRegisterDto.copy(email = "email2", name = "test2"))
        userService.register(mockRegisterDto.copy(email = "email3", name = "teST"))

        // act
        val result: List<UserResponseDto> = userService.searchByName("test").body!!

        // assert
        assertThat(result.size).isEqualTo(2)
    }

    @Test
    fun is_getStickerList_works_well_on_empty() {
        // arrange
        val uid: Long = userService.register(mockRegisterDto)

        // act
        val result: List<Boolean> = userService.getStickerList(uid).body!!

        // assert
        assertThat(result.size).isEqualTo(10)
        for(i in 0..4) {
            assertThat(result[i]).isEqualTo(true)
        }
        for(i in 5..9) {
            assertThat(result[i]).isEqualTo(false)
        }
    }

    @Test
    fun is_getStickerList_works_well() {
        // arrange
        val uid: Long = userService.register(mockRegisterDto)
        userRepository.findById(uid).get().apply {
            sticker = "01101"
            userRepository.save(this)
        }

        // act
        val result: List<Boolean> = userService.getStickerList(uid).body!!

        // assert
        assertThat(result.size).isEqualTo(10)
        for(i in 0..4) {
            assertThat(result[i]).isEqualTo(true)
        }
        assertThat(result[5]).isEqualTo(false)
        assertThat(result[6]).isEqualTo(true)
        assertThat(result[7]).isEqualTo(true)
        assertThat(result[8]).isEqualTo(false)
        assertThat(result[9]).isEqualTo(true)
    }

    @Test
    fun is_isEligibleForSlot_works_well_true() {
        // arrange
        val uid: Long = userService.register(mockRegisterDto)

        // act & assert
        assertThat(userService.isEligibleForSlot(uid).body!!).isEqualTo(true)
    }

    @Test
    fun is_isEligibleForSlot_works_well_false() {
        // arrange
        val uid: Long = userService.register(mockRegisterDto)
        userRepository.findById(uid).get().apply {
            latestSlot = LocalDate.now()
            userRepository.save(this)
        }

        // act & assert
        assertThat(userService.isEligibleForSlot(uid).body!!).isEqualTo(false)
    }
}

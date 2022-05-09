package com.duckdam.service

import com.duckdam.MockDto
import com.duckdam.domain.compliment.ComplimentRepository
import com.duckdam.domain.user.UserRepository
import com.duckdam.dto.compliment.ComplimentRequestDto
import com.duckdam.dto.user.RegisterDto
import com.duckdam.errors.exception.NotFoundException
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.fail
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit.jupiter.SpringExtension

@SpringBootTest
@ExtendWith(SpringExtension::class)
class ComplimentServiceTest {

    @Autowired
    private lateinit var complimentService: ComplimentService

    @Autowired
    private lateinit var userService: UserService

    @Autowired
    private lateinit var complimentRepository: ComplimentRepository

    @Autowired
    private lateinit var userRepository: UserRepository

    private val mockRegisterDto: RegisterDto = MockDto.mockRegisterDto

    @BeforeEach
    @AfterEach
    fun init() {
        userRepository.deleteAll()
        complimentRepository.deleteAll()
    }

    @Test
    fun is_generateCompliment_works_well() {
        // arrange
        val uid1: Long = userService.register(mockRegisterDto)
        val uid2: Long = userService.register(mockRegisterDto.copy(email = "email", name = "new"))
        val complimentRequestDto = ComplimentRequestDto(toId = uid2, stickerNum = 0, message = "thanks")

        // act
        val complimentId: Long = complimentService.generateCompliment(uid1, complimentRequestDto)

        // assert
        complimentRepository.findById(complimentId).get().apply {
            assertThat(fromId).isEqualTo(uid1)
            assertThat(toId).isEqualTo(uid2)
        }
    }

    @Test
    fun is_generateCompliment_throws_invalid_toId() {
        // arrange
        val uid1: Long = userService.register(mockRegisterDto)
        val invalidId: Long = 10
        val complimentRequestDto = ComplimentRequestDto(toId = invalidId, stickerNum = 0, message = "thanks")

        // act & assert
        runCatching {
            complimentService.generateCompliment(uid1, complimentRequestDto)
        }.onSuccess {
            fail("This should be failed.")
        }.onFailure {
            assertThat(it is NotFoundException).isEqualTo(true)
            assertThat(it.message).isEqualTo("User [${invalidId}] was not registered.")
        }
    }
}
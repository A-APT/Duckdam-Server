package com.duckdam.domain

import com.duckdam.domain.user.User
import com.duckdam.domain.user.UserRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit.jupiter.SpringExtension

@SpringBootTest
@ExtendWith(SpringExtension::class)
class UserRepositoryTest {
    @Autowired
    private lateinit var userRepository: UserRepository

    private val mockUser: User = User( // arrange
        name = "je",
        password = "test",
        email = "email@konkuk.ac.kr",
        roles = listOf("ROLE_USER")
    )

    @BeforeEach
    @AfterEach
    fun init() {
        userRepository.deleteAll()
    }

    @Test
    fun is_save_and_singleFind_works_well() {
        // act
        userRepository.save(mockUser)

        // assert
        assertThat(userRepository.findByEmail(mockUser.email).email).isEqualTo(mockUser.email)
    }
}

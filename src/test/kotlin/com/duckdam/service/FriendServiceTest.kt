package com.duckdam.service

import com.duckdam.MockDto
import com.duckdam.domain.friend.FriendRepository
import com.duckdam.domain.user.UserRepository
import com.duckdam.dto.friend.FriendResponseDto
import com.duckdam.dto.user.RegisterDto
import com.duckdam.errors.exception.NotFoundException
import org.assertj.core.api.Assertions
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
class FriendServiceTest {

    @Autowired
    private lateinit var friendService: FriendService

    @Autowired
    private lateinit var userService: UserService

    @Autowired
    private lateinit var friendRepository: FriendRepository

    @Autowired
    private lateinit var userRepository: UserRepository

    private val mockRegisterDto: RegisterDto = MockDto.mockRegisterDto

    @BeforeEach
    @AfterEach
    fun init() {
        friendRepository.deleteAll()
        userRepository.deleteAll()
    }

    @Test
    fun is_followFriend_works_well() {
        // arrange
        val uid1: Long = userService.register(mockRegisterDto)
        val uid2: Long = userService.register(mockRegisterDto.copy(email = "email"))

        // act
        friendService.followFriend(uid1, uid2)

        // assert
        friendRepository.findByUidAndFriendId(uid1, uid2)
        runCatching {
            friendRepository.findByUidAndFriendId(uid2, uid1)
        }.onSuccess {
            Assertions.fail("This should be failed.")
        }
    }

    @Test
    fun is_followFriend_works_on_invalid_targetId() {
        // arrange
        val uid1: Long = userService.register(mockRegisterDto)
        val invalidUid: Long = 10

        // act & assert
        runCatching {
            friendService.followFriend(uid1, invalidUid)
        }.onSuccess {
            Assertions.fail("This should be failed.")
        }.onFailure {
            assertThat(it is NotFoundException).isEqualTo(true)
            assertThat(it.message).isEqualTo("User [$invalidUid] was not registered.")
        }
    }

    @Test
    fun is_findMyFriends_works_well_on_empty() {
        // arrange
        val uid1: Long = userService.register(mockRegisterDto)

        // act
        val friendList: List<FriendResponseDto> = friendService.findMyFriends(uid1).body!!

        // assert
        assertThat(friendList.size).isEqualTo(0)
    }

    @Test
    fun is_findMyFriends_works_well() {
        // arrange
        val uid1: Long = userService.register(mockRegisterDto)
        val uid2: Long = userService.register(mockRegisterDto.copy(email = "email", name="name"))
        val uid3: Long = userService.register(mockRegisterDto.copy(email = "email2", profile = "test"))

        friendService.followFriend(uid1, uid2)
        friendService.followFriend(uid1, uid3)

        // act
        val friendList: List<FriendResponseDto> = friendService.findMyFriends(uid1).body!!

        // assert
        assertThat(friendList.size).isEqualTo(2)
        assertThat(friendList[0].name).isEqualTo("name")
        assertThat(friendList[1].profile).isEqualTo("test")
    }
}

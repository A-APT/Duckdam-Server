package com.duckdam.service

import com.duckdam.domain.friend.Friend
import com.duckdam.domain.friend.FriendRepository
import com.duckdam.domain.user.User
import com.duckdam.domain.user.UserRepository
import com.duckdam.dto.friend.FriendResponseDto
import com.duckdam.errors.exception.NotFoundException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service

@Service
class FriendService (
    private val userRepository: UserRepository,
    private val friendRepository: FriendRepository,
){
    fun followFriend(uid: Long, targetId: Long) {
        runCatching {
            userRepository.findById(targetId).get()
        }.onFailure {
            throw NotFoundException("User [${targetId}] was not registered.")
        }
        friendRepository.save(Friend(uid = uid, friendId = targetId))
    }

    fun findMyFriends(uid: Long): ResponseEntity<List<FriendResponseDto>> {
        val friendList: List<Friend> = friendRepository.findAllByUid(uid)
        val friendResList: MutableList<FriendResponseDto> = mutableListOf()
        friendList.forEach {
            val user: User = userRepository.findById(it.friendId).get()
            friendResList.add(
                FriendResponseDto(
                uid =  user.id,
                name = user.name,
                profile = user.profile
            ))
        }
        return ResponseEntity
            .status(HttpStatus.OK)
            .body(
                friendResList
            )
    }
}

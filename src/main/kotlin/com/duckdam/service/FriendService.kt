package com.duckdam.service

import com.duckdam.domain.friend.Friend
import com.duckdam.domain.friend.FriendRepository
import com.duckdam.domain.user.UserRepository
import com.duckdam.errors.exception.NotFoundException
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
}

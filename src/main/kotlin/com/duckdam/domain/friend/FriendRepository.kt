package com.duckdam.domain.friend

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface FriendRepository: JpaRepository<Friend, Long> {
    fun findByUidAndFriendId(uid: Long, friendId: Long): Friend
    fun findAllByUid(uid: Long): MutableList<Friend>
}

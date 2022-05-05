package com.duckdam.controller

import com.duckdam.dto.friend.FriendResponseDto
import com.duckdam.security.JWTTokenProvider
import com.duckdam.service.FriendService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
class FriendController (
    private val friendService: FriendService,
    private val jwtTokenProvider: JWTTokenProvider,
) {

    @PostMapping("/friend/follow/{targetId}")
    fun followFriend(@RequestHeader httpHeaders: Map<String, String>, @PathVariable targetId: Long): ResponseEntity<Unit> {
        val uid: Long = jwtTokenProvider.getUserPK(jwtTokenProvider.getTokenFromHeader(httpHeaders)!!).toLong()
        friendService.followFriend(uid, targetId)
        return ResponseEntity.noContent().build()
    }

    @GetMapping("/friend")
    fun findMyFriends(@RequestHeader httpHeaders: Map<String, String>): ResponseEntity<List<FriendResponseDto>> {
        val uid: Long = jwtTokenProvider.getUserPK(jwtTokenProvider.getTokenFromHeader(httpHeaders)!!).toLong()
        return friendService.findMyFriends(uid)
    }

}

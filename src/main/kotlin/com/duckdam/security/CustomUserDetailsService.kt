package com.duckdam.security

import com.duckdam.domain.user.UserRepository
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.stereotype.Service

@Service
class CustomUserDetailsService(private val userRepository: UserRepository): UserDetailsService {
    override fun loadUserByUsername(id: String): UserDetails {
        return userRepository.findById(id.toLong()).get()
    }
}
